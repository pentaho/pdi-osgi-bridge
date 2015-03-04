/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
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
import org.pentaho.di.osgi.service.lifecycle.LifecycleEvent;
import org.pentaho.di.osgi.service.lifecycle.OSGIServiceLifecycleListener;
import org.pentaho.osgi.api.BeanFactory;

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

  public DelayedServiceNotifier( OSGIPluginTracker osgiPluginTracker, Class<?> classToTrack,
                                 LifecycleEvent eventType,
                                 Object serviceObject, Map<Class, List<OSGIServiceLifecycleListener>> listeners,
                                 ScheduledExecutorService scheduler ) {
    this.osgiPluginTracker = osgiPluginTracker;
    this.classToTrack = classToTrack;
    this.eventType = eventType;
    this.serviceObject = serviceObject;
    this.listeners = listeners;
    this.scheduler = scheduler;
  }

  @Override
  public void run() {
    BeanFactory factory = osgiPluginTracker.findOrCreateBeanFactoryFor( serviceObject );
    // The beanfactory may not be registered yet. If not schedule a check every second until it is.
    // stopping services won't be able to find a beanfactory. Just skip
    if ( ( factory == null || osgiPluginTracker.getProxyUnwrapper() == null ) && eventType != LifecycleEvent.STOP ) {
      ScheduledFuture<?> timeHandle = scheduler.schedule( this, 100, TimeUnit.MILLISECONDS );
    } else {
      List<OSGIServiceLifecycleListener> listenerList = listeners.get( classToTrack );
      if ( listenerList != null ) {
        for ( OSGIServiceLifecycleListener listener : listenerList ) {
          switch( eventType ) {
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
    }
  }
}
