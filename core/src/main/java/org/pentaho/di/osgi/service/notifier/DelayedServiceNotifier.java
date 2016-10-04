/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.osgi.service.notifier;

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
  private final Map<Class, List<OSGIServiceLifecycleListener>> listeners;
  private final ScheduledExecutorService scheduler;
  private final OSGIPluginTracker osgiPluginTracker;
  private final Class<?> classToTrack;
  private final LifecycleEvent eventType;
  private final Object serviceObject;
  private final DelayedServiceNotifierListener delayedServiceNotifierListener;
  private Logger logger = LoggerFactory.getLogger( DelayedServiceNotifier.class );


  public DelayedServiceNotifier( OSGIPluginTracker osgiPluginTracker, Class<?> classToTrack,
                                 LifecycleEvent eventType,
                                 Object serviceObject, Map<Class, List<OSGIServiceLifecycleListener>> listeners,
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
        List<OSGIServiceLifecycleListener> listenerList = listeners.get( classToTrack );
        if ( listenerList != null ) {
          for ( OSGIServiceLifecycleListener listener : listenerList ) {
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
          }
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
