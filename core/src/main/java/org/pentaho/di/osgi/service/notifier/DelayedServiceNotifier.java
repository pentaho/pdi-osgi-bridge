/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.osgi.service.notifier;

import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.osgi.OSGIPluginTracker;
import org.pentaho.di.osgi.OSGIPluginTrackerException;
import org.pentaho.di.osgi.service.lifecycle.LifecycleEvent;
import org.pentaho.di.osgi.service.lifecycle.OSGIServiceLifecycleListener;
import org.pentaho.osgi.api.BeanFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by bryan on 8/15/14.
 */
public class DelayedServiceNotifier implements Runnable {
  private final Map<Class, OSGIServiceLifecycleListener> listeners;
  private final ScheduledExecutorService scheduler;
  private final OSGIPluginTracker osgiPluginTracker;
  private final Class<?> classToTrack;
  private final LifecycleEvent eventType;
  private final Object serviceObject;
  private final DelayedServiceNotifierListener delayedServiceNotifierListener;
  private Logger logger = LoggerFactory.getLogger( DelayedServiceNotifier.class );


  public DelayedServiceNotifier( OSGIPluginTracker osgiPluginTracker, Class<?> classToTrack,
                                 LifecycleEvent eventType,
                                 Object serviceObject, Map<Class, OSGIServiceLifecycleListener> listeners,
                                 ScheduledExecutorService scheduler,
                                 DelayedServiceNotifierListener delayedServiceNotifierListener ) {
    this.osgiPluginTracker = osgiPluginTracker;
    this.classToTrack = classToTrack;
    this.eventType = eventType;
    this.serviceObject = serviceObject;
    this.listeners = listeners;
    this.scheduler = scheduler;
    this.delayedServiceNotifierListener = delayedServiceNotifierListener;
  }

  @Override
  public void run() {
    BeanFactory factory = null;
    try {
      factory = osgiPluginTracker.findOrCreateBeanFactoryFor( serviceObject );
    } catch ( OSGIPluginTrackerException e ) {
      logger.debug( "Error in the plugin tracker. We cannot proceed.", e );
      notifyListener();
      return;
    } catch ( Exception e ) {
      logger.debug( "Error trying to notify on service registration", e );
      notifyListener();
      return;
    }
    // The beanfactory may not be registered yet. If not schedule a check every second until it is.
    // stopping services won't be able to find a beanfactory. Just skip
    if ( ( factory == null || osgiPluginTracker.getProxyUnwrapper() == null ) && eventType != LifecycleEvent.STOP ) {
      ScheduledFuture<?> timeHandle = scheduler.schedule( this, 100, TimeUnit.MILLISECONDS );
    } else {
      try {
        OSGIServiceLifecycleListener listener = listeners.get( classToTrack );
        if ( listener != null ) {
          switch ( eventType ) {
            case START:
              listener.pluginAdded( serviceObject );
              break;
            case STOP:
              listener.pluginRemoved( serviceObject );
              break;
            case MODIFY:
              listener.pluginChanged( serviceObject );
              break;
            default:
              throw new IllegalStateException( "Unhandled enum value: " + eventType );
          }
        } else if ( classToTrack.isAssignableFrom( PluginInterface.class ) ) {
          // if there was no listener, retry later
          logger.debug( "No listener for PluginInterface to handle " + classToTrack.getName() );
          ScheduledFuture<?> timeHandle = scheduler.schedule( this, 100, TimeUnit.MILLISECONDS );
        }
      } catch ( IllegalStateException e ) {
        if ( e.getMessage().startsWith( "Invalid BundleContext" ) ) {
          // try again later; bundle is restarting
          ScheduledFuture<?> timeHandle = scheduler.schedule( this, 100, TimeUnit.MILLISECONDS );
        }
      } finally {
        notifyListener();
      }
    }
  }

  public void notifyListener() {
    // Notify the listener so he's not waiting for us, we're done here
    if ( delayedServiceNotifierListener != null ) {
      delayedServiceNotifierListener.onRun( eventType, serviceObject );
    }
  }
}
