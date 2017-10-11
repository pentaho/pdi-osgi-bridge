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
