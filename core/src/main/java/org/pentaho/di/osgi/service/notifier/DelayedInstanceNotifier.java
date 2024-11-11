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

import org.pentaho.di.osgi.OSGIPluginTracker;
import org.pentaho.di.osgi.OSGIPluginTrackerException;
import org.pentaho.di.osgi.ServiceReferenceListener;
import org.pentaho.di.osgi.service.lifecycle.LifecycleEvent;
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
public class DelayedInstanceNotifier implements Runnable {
  private final Map<Object, List<ServiceReferenceListener>> instanceListeners;
  private final ScheduledExecutorService scheduler;
  private final OSGIPluginTracker osgiPluginTracker;
  private final LifecycleEvent eventType;
  private final Object serviceObject;
  private final Logger logger = LoggerFactory.getLogger( getClass() );

  public DelayedInstanceNotifier( OSGIPluginTracker osgiPluginTracker, LifecycleEvent eventType,
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
    BeanFactory factory = null;
    try {
      factory = osgiPluginTracker.findOrCreateBeanFactoryFor( serviceObject );
    } catch ( OSGIPluginTrackerException e ) {
      logger.warn( "Error getting bean factory. If this error does not self-correct, there may be other startup problems.", e );
    }
    if ( factory == null ) {
      ScheduledFuture<?> timeHandle = scheduler.schedule( this, 2, TimeUnit.SECONDS );
    } else {
      List<ServiceReferenceListener> listeners = instanceListeners.get( serviceObject );
      if ( listeners != null ) {
        for ( ServiceReferenceListener listener : listeners ) {
          listener.serviceEvent( eventType, serviceObject );
        }
      }
    }
  }
}
