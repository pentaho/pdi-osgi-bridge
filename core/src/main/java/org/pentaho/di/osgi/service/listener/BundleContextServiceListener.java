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


package org.pentaho.di.osgi.service.listener;

import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.pentaho.di.osgi.service.lifecycle.LifecycleEvent;
import org.pentaho.di.osgi.service.notifier.DelayedInstanceNotifierFactory;

import java.util.Map;

/**
 * Created by bryan on 8/18/14.
 */
public class BundleContextServiceListener implements ServiceListener {
  private final Map<ServiceReference, Object> referenceToInstanceMap;
  private final DelayedInstanceNotifierFactory delayedInstanceNotifierFactory;

  public BundleContextServiceListener( Map<ServiceReference, Object> referenceToInstanceMap,
                                       DelayedInstanceNotifierFactory delayedInstanceNotifierFactory ) {
    this.referenceToInstanceMap = referenceToInstanceMap;
    this.delayedInstanceNotifierFactory = delayedInstanceNotifierFactory;
  }

  @Override
  public void serviceChanged( ServiceEvent serviceEvent ) {
    if ( referenceToInstanceMap.containsKey( serviceEvent.getServiceReference() ) ) {
      Object instance = referenceToInstanceMap.get( serviceEvent.getServiceReference() );
      LifecycleEvent type = LifecycleEvent.MODIFY;

      switch( serviceEvent.getType() ) {
        case ServiceEvent.MODIFIED:
          type = LifecycleEvent.MODIFY;
          break;
        case ServiceEvent.UNREGISTERING:
          type = LifecycleEvent.STOP;
          break;
      }
      delayedInstanceNotifierFactory.create( instance, type ).run();
    }
  }
}
