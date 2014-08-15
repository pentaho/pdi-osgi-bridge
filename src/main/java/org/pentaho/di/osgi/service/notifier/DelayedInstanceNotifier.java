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
import org.pentaho.osgi.api.BeanFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by bryan on 8/15/14.
 */
public class DelayedInstanceNotifier implements Runnable {
  private final Map<Object, List<ServiceReferenceListener>> instanceListeners;
  private final ScheduledExecutorService scheduler;
  private OSGIPluginTracker osgiPluginTracker;
  private ServiceReferenceListener.EVENT_TYPE eventType;
  private Object serviceObject;

  public DelayedInstanceNotifier( OSGIPluginTracker osgiPluginTracker, ServiceReferenceListener.EVENT_TYPE eventType,
                                  Object serviceObject, Map<Object, List<ServiceReferenceListener>> instanceListeners,
                                  ScheduledExecutorService scheduler ) {
    this.osgiPluginTracker = osgiPluginTracker;
    this.eventType = eventType;
    this.serviceObject = serviceObject;
    this.instanceListeners = instanceListeners;
    this.scheduler = scheduler;
  }

  @Override
  public void run() {
    List<ServiceReferenceListener> listeners = instanceListeners.get( serviceObject );
    BeanFactory factory = osgiPluginTracker.findOrCreateBeanFactoryFor( serviceObject );
    if ( factory == null ) {
      ScheduledFuture<?> timeHandle =
        scheduler.schedule(
          new DelayedInstanceNotifier( osgiPluginTracker, eventType, serviceObject, instanceListeners, scheduler ), 2,
          TimeUnit.SECONDS );
    } else {
      for ( ServiceReferenceListener listener : listeners ) {
        listener.serviceEvent( eventType, serviceObject );
      }
    }
  }
}
