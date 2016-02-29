package org.pentaho.di.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.pentaho.di.core.util.ExecutorUtil;
import org.pentaho.osgi.api.IKarafBlueprintWatcher;
import org.pentaho.osgi.api.IKarafFeatureWatcher;
import org.pentaho.platform.servicecoordination.api.IPhasedLifecycleEvent;
import org.pentaho.platform.servicecoordination.api.IPhasedLifecycleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by nbaker on 2/19/15.
 */
public class KarafLifecycleListener implements IPhasedLifecycleListener<KettleLifecycleEvent> {
  private static KarafLifecycleListener INSTANCE = new KarafLifecycleListener( );
  private static Logger logger = LoggerFactory.getLogger( KarafLifecycleListener.class );
  private final long timeout;
  private AtomicBoolean listenerActive = new AtomicBoolean( false );
  private AtomicBoolean initialized = new AtomicBoolean( false );
  private BundleContext bundleContext;
  private IPhasedLifecycleEvent<KettleLifecycleEvent> event;

  KarafLifecycleListener() {
    this( calculateTimeout() );
  }

  KarafLifecycleListener( long timeout ) {
    this.timeout = timeout;
  }

  private static long calculateTimeout() {
    long result = TimeUnit.SECONDS.toMillis( 100 );
    String timeoutProp = System.getProperty( KarafLifecycleListener.class.getCanonicalName() + ".timeout" );
    if ( timeoutProp != null ) {
      try {
        result = Long.parseLong( timeoutProp );
      } catch ( Exception e ) {
        logger.warn( "Failed to parse custom timeout property of " + timeoutProp + ", returning default of 100,000" );
      }
    }
    return result;
  }

  public static KarafLifecycleListener getInstance() {
    return INSTANCE;
  }

  @Override public void onPhaseChange( final IPhasedLifecycleEvent<KettleLifecycleEvent> event ) {
    this.event = event;
    if ( event.getNotificationObject().equals( KettleLifecycleEvent.INIT ) ) {
      listenerActive.set( true );
      startTimeoutThread();
      maybeStartWatchers();
    } else {
      // simple accept all other events
      event.accept();
    }
  }

  private void startTimeoutThread() {
    // start watch thread to prevent deadlock where the event is never accepted
    Thread t = new Thread( new Runnable() {

      @Override protected void finalize() throws Throwable {
        super.finalize();
      }

      @Override public void run() {
        long endWaitTime = System.currentTimeMillis() + timeout;
        while ( !initialized.get() && ( endWaitTime - System.currentTimeMillis() ) > 0 ) {
          try {
            Thread.sleep( 100 );
          } catch ( InterruptedException e ) {
            return;
          }
        }
        if ( !initialized.get() ) {
          // We fell out due to time or an exception. Ensure that we release the lifecycle hold
          logger.error(
              "The Kettle Karaf Lifecycle Listener failed to execute properly after waiting for " + TimeUnit.MILLISECONDS.toSeconds(timeout)
                      + " seconds. Releasing lifecycle hold, but some services may be unavailable." );
          event.accept();
        }
      }
    } );
    t.setDaemon( true );
    t.setName( "KarafLifecycleListener Timeout Thread" );
    t.start();
  }

  private void maybeStartWatchers() {
    if ( bundleContext != null && listenerActive.get() ) {

      Thread thread = new Thread( new Runnable() {
        @Override public void run() {
          ServiceReference<IKarafFeatureWatcher> featureWatcherServiceReference =
              bundleContext.getServiceReference( IKarafFeatureWatcher.class );
          try {
            if ( featureWatcherServiceReference == null ) {
              throw new IKarafFeatureWatcher.FeatureWatcherException( "No IKarafFeatureWatcher service available" );
            }
            IKarafFeatureWatcher karafFeatureWatcher = bundleContext.getService( featureWatcherServiceReference );
            karafFeatureWatcher.waitForFeatures();
          } catch ( IKarafFeatureWatcher.FeatureWatcherException e ) {
            logger.error( "Error in Feature Watcher", e );

            // We're not going to kill the system in the case of Feature errors, for now.
            //event.exception( e );
          }

          ServiceReference<IKarafBlueprintWatcher>
              blueprintWatcherServiceReference =
              bundleContext.getServiceReference( IKarafBlueprintWatcher.class );
          try {
            if ( blueprintWatcherServiceReference == null ) {
              throw new IKarafBlueprintWatcher.BlueprintWatcherException(
                  "No IKarafBlueprintWatcher service available" );
            }
            IKarafBlueprintWatcher karafBlueprintWatcher = bundleContext.getService( blueprintWatcherServiceReference );
            karafBlueprintWatcher.waitForBlueprint();
          } catch ( IKarafBlueprintWatcher.BlueprintWatcherException e ) {
            logger.error( "Error in Blueprint Watcher", e );
          }

          event.accept();
        }
      } );
      thread.setDaemon( true );
      thread.start();
      initialized.set( true );

    }
  }

  public void setBundleContext( BundleContext bundleContext ) {

    this.bundleContext = bundleContext;
    bundleContext.registerService( ExecutorService.class, ExecutorUtil.getExecutor(), new Hashtable<String, Object>() );
    maybeStartWatchers();

  }
}
