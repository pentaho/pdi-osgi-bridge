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
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.startlevel.FrameworkStartLevel;
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
import java.util.function.Function;

/**
 * Created by nbaker on 2/19/15.
 */
public class KarafLifecycleListener implements IPhasedLifecycleListener<KettleLifecycleEvent> {
  private static KarafLifecycleListener instance;
  private static Logger logger = LoggerFactory.getLogger( KarafLifecycleListener.class );
  private final long timeout;
  private final OSGIPluginTracker osgiPluginTracker;
  private AtomicBoolean listenerActive = new AtomicBoolean( false );
  private AtomicBoolean initialized = new AtomicBoolean( false );
  private BundleContext bundleContext;
  private IPhasedLifecycleEvent<KettleLifecycleEvent> event;
  private Thread watcherThread;

  private final Integer frameworkBeginningStartLevel;
  private FrameworkStartLevel frameworkStartLevel;

  @VisibleForTesting
  KarafLifecycleListener() {
    this( getSystemProperty( KarafLifecycleListener.class.getCanonicalName() + ".timeout", TimeUnit.SECONDS.toMillis( 100 ), Long::parseLong ) );
  }

  @VisibleForTesting
  KarafLifecycleListener( long timeout ) {
    this( timeout, OSGIPluginTracker.getInstance() );
  }

  @VisibleForTesting
  KarafLifecycleListener( long timeout, OSGIPluginTracker osgiPluginTracker ) {
    this( timeout, osgiPluginTracker, getSystemProperty( Constants.FRAMEWORK_BEGINNING_STARTLEVEL, 100, Integer::parseInt ) );
  }

  @VisibleForTesting
  KarafLifecycleListener( long timeout, OSGIPluginTracker osgiPluginTracker, int frameworkBeginningStartLevel ) {
    this.timeout = timeout;
    this.osgiPluginTracker = osgiPluginTracker;
    this.frameworkBeginningStartLevel = frameworkBeginningStartLevel;
  }

  private static <T> T getSystemProperty( String propertyKey, T defaultValue, Function<String, T> parseFunction ) {
    String propertyValue = System.getProperty( propertyKey );
    T result = defaultValue;
    try {
      result = parseFunction.apply( propertyValue );
    } catch ( Exception e ) {
      logger.debug( "Failed to parse {} property of value {}, returning default value of {}.", propertyKey, propertyValue, defaultValue );
    }
    return result;
  }

  public static synchronized KarafLifecycleListener getInstance() {
    if ( instance == null ) {
      instance = new KarafLifecycleListener();
    }
    return instance;
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
    final long endWaitTime = System.currentTimeMillis() + timeout;

    // start watch thread to prevent deadlock where the event is never accepted
    Thread t = new Thread( new Runnable() {

      @Override public void run() {
        while ( !initialized.get() && !timedOut() ) {
          try {
            Thread.sleep( 100 );
          } catch ( InterruptedException e ) {
            Thread.currentThread().interrupt();
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

      private boolean timedOut() {
        // when timeout is negative consider that it never ends
        if ( timeout < 0 ) {
          return false;
        }
        return System.currentTimeMillis() > endWaitTime;
      }

    } );
    t.setDaemon( true );
    t.setName( "KarafLifecycleListener Timeout Thread" );
    t.start();
  }


  private void maybeStartWatchers() {
    if ( bundleContext != null && listenerActive.get() ) {

      watcherThread = new Thread( () -> {
        waitForBundlesStarted();
        waitForBlueprints();
        acceptEventOnDelayedServiceNotifiersDone();
      } );
      watcherThread.setDaemon( true );
      watcherThread.start();
      initialized.set( true );
    }
  }

  /**
   * Actively wait until bundles from startup.properties, from boot features and Pentaho runtime features are started.
   *
   * Because waitForFeatures works with empty caches and waitForFrameworkStarted only indicates "all" bundles for full caches
   * we need both to make sure all pertinent bundles have started in all boot scenarios.
   */
  private void waitForBundlesStarted() {
    waitForFeatures();
    waitForFrameworkStarted();
  }

  private synchronized <T> T getOsgiService( Class<T> serviceClass ) {
    if ( null != bundleContext ) {
      ServiceReference<T> serviceReference = bundleContext.getServiceReference( serviceClass );
      if ( serviceReference == null ) {
        return null;
      }
      return bundleContext.getService( serviceReference );
    } else {
      return null;
    }
  }

  /**
   * Actively wait until features are installed.
   *
   * The current used implementation of IKarafFeatureWatcher, which is org.pentaho.osgi.impl.KarafFeatureWatcherImpl,
   * is only properly functioning when booting Karaf with empty caches. This is because of a behaviour (bug?) of the
   * Karaf Feature Service that, when booting with full caches, hydrates the persisted state with all features marked
   * as installed and started. As such, when asking the Feature Service if a given feature is installed, it will
   * reply positively even if its bundles haven't been started yet.
   */
  private void waitForFeatures() {
    IKarafFeatureWatcher karafFeatureWatcher = getOsgiService( IKarafFeatureWatcher.class );
    try {
      if ( karafFeatureWatcher == null ) {
        if ( null != bundleContext ) {
          throw new IKarafFeatureWatcher.FeatureWatcherException( "No IKarafFeatureWatcher service available." );
        } // no-op if bundle is stopped
      } else {
        karafFeatureWatcher.waitForFeatures();
      }
    } catch ( IKarafFeatureWatcher.FeatureWatcherException e ) {
      if ( null != bundleContext ) {
        logger.error( "Error in Feature Watcher", e );
      } // no-op if bundle is stopped
    }
  }

  private void waitForBlueprints() {
    IKarafBlueprintWatcher karafBlueprintWatcher = getOsgiService( IKarafBlueprintWatcher.class );
    try {
      if ( karafBlueprintWatcher == null ) {
        if ( null != bundleContext ) {
          throw new IKarafBlueprintWatcher.BlueprintWatcherException( "No IKarafBlueprintWatcher service available." );
        } // no-op if bundle is stopped
      } else {
        karafBlueprintWatcher.waitForBlueprint();
      }
    } catch ( IKarafBlueprintWatcher.BlueprintWatcherException e ) {
      if ( null != bundleContext ) {
        logger.error( "Error in Blueprint Watcher", e );
      } // no-op if bundle is stopped
    }
  }

  private void acceptEventOnDelayedServiceNotifiersDone() {
    if ( null != bundleContext ) {
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
  }

  /**
   * Actively wait until the OSGi framework has started.
   *
   * When booting Karaf with empty caches it only waits until the bundles defined in the startup.properties have started,
   * while when booting with full caches it waits until all cached bundles have started.
   */
  private void waitForFrameworkStarted() {
    /* According to OSGi Core specs V6.0 Section 9.3.2, the OSGi framework should broadcast a FrameworkEvent.STARTED
       event when the beginning start level is reached. According to the example in 9.4.2, this event can be used to
       to determine that the system has been initialized.
       Unfortunately, our current used OSGi framework, Felix framework Version 5.6.12, is currently immediately firing
       the event on start: https://github.com/apache/felix/blob/3bf3c664eb64aef08df9968d1099b51c4c300ff8/src/main/java/org/apache/felix/framework/Felix.java#L999
       As such, we're directly checking the current framework start level to verify if it has reached the framework
       beginning start level to determine if the framework has started and consequently the system has been initialized. */
    while ( frameworkStartLevel.getStartLevel() < frameworkBeginningStartLevel ) {
      try {
        Thread.sleep( 100 );
      } catch ( InterruptedException e ) {
        logger.debug( "Thread interrupted while waiting for OSGi framework start level to reach the beginning start level." );
        Thread.currentThread().interrupt();
      }
    }
  }

  public synchronized void setBundleContext( BundleContext bundleContext ) {
    this.bundleContext = bundleContext;
    if ( null != bundleContext ) {
      logger.debug( "Bundle context set in KarafLifecycleListener" );
      bundleContext.registerService( ExecutorService.class, ExecutorUtil.getExecutor(), new Hashtable<>() );
      this.frameworkStartLevel = bundleContext.getBundle( 0 ).adapt( FrameworkStartLevel.class );
      maybeStartWatchers();
    } else {
      logger.debug( "Bundle context cleared in KarafLifecycleListener" );
      if ( null != watcherThread && watcherThread.isAlive() ) {
        logger.debug( "Watcher thread interrupted" );
        watcherThread.interrupt();
        watcherThread = null;
      }
    }
  }
}
