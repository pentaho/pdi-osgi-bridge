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

package org.pentaho.di.osgi.service.notifier;

import org.pentaho.di.osgi.OSGIPluginTracker;
import org.pentaho.di.osgi.ServiceReferenceListener;
import org.pentaho.di.osgi.service.lifecycle.LifecycleEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by bryan on 8/18/14.
 */
public class DelayedInstanceNotifierFactory {
  private final Map<Object, List<ServiceReferenceListener>> instanceListeners;
  private final ScheduledExecutorService scheduler;
  private final OSGIPluginTracker osgiPluginTracker;

  public DelayedInstanceNotifierFactory( Map<Object, List<ServiceReferenceListener>> instanceListeners,
                                         ScheduledExecutorService scheduler, OSGIPluginTracker osgiPluginTracker ) {
    this.instanceListeners = instanceListeners;
    this.scheduler = scheduler;
    this.osgiPluginTracker = osgiPluginTracker;
  }

  public DelayedInstanceNotifier create( Object instance, LifecycleEvent event_type ) {
    return new DelayedInstanceNotifier( osgiPluginTracker, event_type, instance, instanceListeners, scheduler );
  }
}
