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
        logger.debug( "Watcher thread started" );
        waitForBundlesStarted();
        waitForBlueprints();
        acceptEventOnDelayedServiceNotifiersDone();
      } );
      watcherThread.setDaemon( true );
      watcherThread.setName( "KarafLifecycleListener Watcher Thread" );
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
    ServiceReference<T> serviceReference = null;
    try {
      while ( null == serviceReference && null != bundleContext && !Thread.currentThread().isInterrupted() ) {
        this.wait( 100 );
        serviceReference = bundleContext.getServiceReference( serviceClass );
      }
    } catch ( InterruptedException | IllegalStateException e ) {
      if ( e instanceof InterruptedException || ( (IllegalStateException) e ).getMessage().startsWith( "Invalid BundleContext" ) ) {
        logger.debug( String.format( "Watcher thread interrupted waiting for service %s", serviceClass.getName() ) );
        Thread.currentThread().interrupt();
      }
      serviceReference = null; // ensure we return null; this thread should die
    }
    if ( null != serviceReference ) {
      try {
        return bundleContext.getService( serviceReference );
      } catch ( IllegalStateException e ) {
        if ( e.getMessage().startsWith( "Invalid BundleContext" ) ) {
          logger.debug( String.format( "Watcher thread interrupted waiting for service %s", serviceClass.getName() ) );
          Thread.currentThread().interrupt();
        }
        return null;
      }
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
  @VisibleForTesting
  void waitForFeatures() {
    try {
      Thread.sleep( 100 );
      IKarafFeatureWatcher karafFeatureWatcher = getOsgiService( IKarafFeatureWatcher.class );
      if ( karafFeatureWatcher == null ) {
        if ( null != bundleContext && !Thread.currentThread().isInterrupted() ) {
          throw new IKarafFeatureWatcher.FeatureWatcherException( "No IKarafFeatureWatcher service available." );
        } else if ( Thread.currentThread().isInterrupted() ) {
          logger.debug( "Thread interrupted itself because bundle context was invalid; bundle likely restarting" );
        }
      } else {
        karafFeatureWatcher.waitForFeatures();
      }
    } catch ( IKarafFeatureWatcher.FeatureWatcherException e ) {
      if ( null != bundleContext && !( e.getCause() instanceof InterruptedException ) ) {
        logger.error( "Error in Feature Watcher", e );
      } else if ( e.getCause() instanceof InterruptedException ) {
        logger.debug( "Watcher thread interrupted during karafFeatureWatcher.waitForFeatures" );
        Thread.currentThread().interrupt();
      }
    } catch ( InterruptedException e ) {
      logger.debug( "Watcher thread interrupted during waitForFeatures" );
      Thread.currentThread().interrupt();
    }
  }

  @VisibleForTesting
  void waitForBlueprints() {
    try {
      Thread.sleep( 100 );
      IKarafBlueprintWatcher karafBlueprintWatcher = getOsgiService( IKarafBlueprintWatcher.class );
      if ( karafBlueprintWatcher == null ) {
        if ( null != bundleContext && !Thread.currentThread().isInterrupted() ) {
          throw new IKarafBlueprintWatcher.BlueprintWatcherException( "No IKarafBlueprintWatcher service available." );
        } else if ( Thread.currentThread().isInterrupted() ) {
          logger.debug( "Thread interrupted itself because bundle context was invalid; bundle likely restarting" );
        }
      } else {
        karafBlueprintWatcher.waitForBlueprint();
      }
    } catch ( IKarafBlueprintWatcher.BlueprintWatcherException e ) {
      if ( null != bundleContext && !( e.getCause() instanceof InterruptedException ) ) {
        logger.error( "Error in Feature Watcher", e );
      } else if ( e.getCause() instanceof InterruptedException ) {
        logger.debug( "Watcher thread interrupted during karafBlueprintWatcher.waitForBlueprint" );
        Thread.currentThread().interrupt();
      }
    } catch ( InterruptedException e ) {
      logger.debug( "Watcher thread interrupted during waitForBlueprints" );
      Thread.currentThread().interrupt();
    }
  }

  @VisibleForTesting
  void acceptEventOnDelayedServiceNotifiersDone() {
    try {
      Thread.sleep( 100 );
      if ( null != bundleContext && !Thread.currentThread().isInterrupted() ) {
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
    } catch ( InterruptedException e ) {
      logger.debug( "Watcher thread interrupted during acceptEventOnDelayedServiceNotifiersDone" );
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Actively wait until the OSGi framework has started.
   *
   * When booting Karaf with empty caches it only waits until the bundles defined in the startup.properties have started,
   * while when booting with full caches it waits until all cached bundles have started.
   */
  @VisibleForTesting
  void waitForFrameworkStarted() {
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
    if ( null != bundleContext ) {
      this.bundleContext = bundleContext;
      logger.debug( "Bundle context set in KarafLifecycleListener" );
      bundleContext.registerService( ExecutorService.class, ExecutorUtil.getExecutor(), new Hashtable<>() );
      this.frameworkStartLevel = bundleContext.getBundle( 0 ).adapt( FrameworkStartLevel.class );
      maybeStartWatchers();
    } else {
      logger.debug( "Bundle context cleared in KarafLifecycleListener" );
      if ( null != watcherThread && watcherThread.isAlive() ) {
        watcherThread.interrupt();
        logger.debug( "Watcher thread interrupted" );
        while ( watcherThread.isAlive() ) {
          try {
            // give the thread a chance to get interrupted and stop before pulling out the bundle context
            this.wait( 100 );
          } catch ( InterruptedException e ) {
            Thread.currentThread().interrupt();
          }
        }
        watcherThread = null;
        this.bundleContext = null;
      }
    }
  }

  @VisibleForTesting
  static void setLogger( Logger testLogger ) {
    logger = testLogger;
  }
}
