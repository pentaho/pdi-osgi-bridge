/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
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
