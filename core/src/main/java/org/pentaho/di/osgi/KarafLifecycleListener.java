/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.osgi;

import com.google.common.annotations.VisibleForTesting;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.pentaho.di.core.util.ExecutorUtil;
import org.pentaho.di.osgi.service.lifecycle.LifecycleEvent;
import org.pentaho.di.osgi.service.notifier.DelayedServiceNotifierListener;
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
  private final OSGIPluginTracker osgiPluginTracker;
  private AtomicBoolean listenerActive = new AtomicBoolean( false );
  private AtomicBoolean initialized = new AtomicBoolean( false );
  private BundleContext bundleContext;
  private IPhasedLifecycleEvent<KettleLifecycleEvent> event;

  @VisibleForTesting
  KarafLifecycleListener() {
    this( calculateTimeout() );
  }

  @VisibleForTesting
  KarafLifecycleListener( long timeout ) {
    this( timeout, OSGIPluginTracker.getInstance() );
  }

  @VisibleForTesting
  KarafLifecycleListener( long timeout, OSGIPluginTracker osgiPluginTracker ) {
    this.timeout = timeout;
    this.osgiPluginTracker = osgiPluginTracker;
  }

  private static long calculateTimeout() {
    long result = TimeUnit.SECONDS.toMillis( 100 );
    String timeoutProp = System.getProperty( KarafLifecycleListener.class.getCanonicalName() + ".timeout" );
    if ( timeoutProp != null ) {
      try {
        result = Long.parseLong( timeoutProp );
      } catch ( Exception e ) {
        logger.warn( "Failed to parse custom timeout property of {}, returning default of 100,000", timeoutProp );
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
            "The Kettle Karaf Lifecycle Listener failed to execute properly after waiting for {} seconds. Releasing lifecycle hold, but some services may be unavailable.",
            TimeUnit.MILLISECONDS.toSeconds( timeout ) );
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

      Thread thread = new Thread( () -> {
        waitForFeatures();
        waitForBlueprints();
        acceptEventOnDelayedServiceNotifiersDone();
      } );
      thread.setDaemon( true );
      thread.start();
      initialized.set( true );
    }
  }


  private <T> T getOsgiService( Class<T> serviceClass ) {
    ServiceReference<T> serviceReference = bundleContext.getServiceReference( serviceClass );
    if ( serviceReference == null ) {
      return null;
    }
    return bundleContext.getService( serviceReference );
  }

  private void waitForFeatures() {
    IKarafFeatureWatcher karafFeatureWatcher = getOsgiService( IKarafFeatureWatcher.class );
    try {
      if ( karafFeatureWatcher == null ) {
        throw new IKarafFeatureWatcher.FeatureWatcherException( "No IKarafFeatureWatcher service available." );
      }
      karafFeatureWatcher.waitForFeatures();
    } catch ( IKarafFeatureWatcher.FeatureWatcherException e ) {
      logger.error( "Error in Feature Watcher", e );
    }
  }

  private void waitForBlueprints() {
    IKarafBlueprintWatcher karafBlueprintWatcher = getOsgiService( IKarafBlueprintWatcher.class );
    try {
      if( karafBlueprintWatcher == null ) {
        throw new IKarafBlueprintWatcher.BlueprintWatcherException( "No IKarafBlueprintWatcher service available." );
      }
      karafBlueprintWatcher.waitForBlueprint();
    } catch ( IKarafBlueprintWatcher.BlueprintWatcherException e ) {
      logger.error( "Error in Blueprint Watcher", e );
    }
  }

  private void acceptEventOnDelayedServiceNotifiersDone() {
    final AtomicBoolean accepted = new AtomicBoolean( false );
    DelayedServiceNotifierListener delayedServiceNotifierListener = new DelayedServiceNotifierListener() {
      @Override public void onRun( LifecycleEvent lifecycleEvent, Object serviceObject ) {
        if ( osgiPluginTracker.getOutstandingServiceNotifierListeners() == 0 && !accepted.getAndSet( true ) ) {
          logger.debug( "Done waiting on delayed service notifiers" );
          event.accept();
          osgiPluginTracker.removeDelayedServiceNotifierListener( this );
        }
      }
    };

    logger.debug( "About to start waiting on delayed service notifiers" );
    osgiPluginTracker.addDelayedServiceNotifierListener( delayedServiceNotifierListener );
    delayedServiceNotifierListener.onRun( null, null );

  }

  public void setBundleContext( BundleContext bundleContext ) {

    this.bundleContext = bundleContext;
    bundleContext.registerService( ExecutorService.class, ExecutorUtil.getExecutor(), new Hashtable<>() );
    maybeStartWatchers();

  }
}
