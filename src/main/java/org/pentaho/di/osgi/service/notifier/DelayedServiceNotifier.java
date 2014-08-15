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
import org.pentaho.di.osgi.ServiceReferenceListener;
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
  private OSGIPluginTracker osgiPluginTracker;
  private Class<?> classToTrack;
  private ServiceReferenceListener.EVENT_TYPE eventType;
  private Object serviceObject;

  public DelayedServiceNotifier( OSGIPluginTracker osgiPluginTracker, Class<?> classToTrack,
                                 ServiceReferenceListener.EVENT_TYPE eventType,
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
    if ( factory == null ) {
      ScheduledFuture<?> timeHandle =
        scheduler.schedule( new DelayedServiceNotifier( osgiPluginTracker, classToTrack, eventType, serviceObject,
            listeners, scheduler ), 2,
          TimeUnit.SECONDS );
    } else {
      for ( OSGIServiceLifecycleListener listener : listeners.get( classToTrack ) ) {
        switch( eventType ) {

          case STARTING:
            listener.pluginAdded( serviceObject );
            break;
          case STOPPING:
            listener.pluginRemoved( serviceObject );
            break;
          case MODIFIED:
            listener.pluginChanged( serviceObject );
            break;
        }
      }
    }
  }
}
