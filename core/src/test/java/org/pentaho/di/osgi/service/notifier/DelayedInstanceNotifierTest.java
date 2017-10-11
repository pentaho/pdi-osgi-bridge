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

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.osgi.OSGIPluginTracker;
import org.pentaho.di.osgi.OSGIPluginTrackerException;
import org.pentaho.di.osgi.ServiceReferenceListener;
import org.pentaho.di.osgi.service.lifecycle.LifecycleEvent;
import org.pentaho.osgi.api.BeanFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

/**
 * Created by bryan on 8/18/14.
 */
public class DelayedInstanceNotifierTest {
  private Map<Object, List<ServiceReferenceListener>> instanceListeners;
  private ScheduledExecutorService scheduler;
  private OSGIPluginTracker osgiPluginTracker;
  private LifecycleEvent eventType;
  private Object serviceObject;

  @Before
  public void setup() {
    instanceListeners = new HashMap<Object, List<ServiceReferenceListener>>();
    scheduler = mock( ScheduledExecutorService.class );
    osgiPluginTracker = mock( OSGIPluginTracker.class );
    eventType = LifecycleEvent.START;
    serviceObject = mock( Object.class );
  }

  @Test
  public void testFactoryEqualNotNull() throws Exception {
    DelayedInstanceNotifier delayedInstanceNotifier =
        new DelayedInstanceNotifier( osgiPluginTracker, eventType, serviceObject, instanceListeners, scheduler );
    ServiceReferenceListener listener = mock( ServiceReferenceListener.class );
    List<ServiceReferenceListener> listeners = new ArrayList<ServiceReferenceListener>( Arrays.asList( listener ) );
    instanceListeners.put( serviceObject, listeners );
    BeanFactory beanFactory = mock( BeanFactory.class );
    when( osgiPluginTracker.findOrCreateBeanFactoryFor( serviceObject ) ).thenReturn( beanFactory );
    delayedInstanceNotifier.run();
    verify( listener ).serviceEvent( eventType, serviceObject );
  }

  @Test
  public void testFactoryEqualNull() throws Exception {
    DelayedInstanceNotifier delayedInstanceNotifier =
        new DelayedInstanceNotifier( osgiPluginTracker, eventType, serviceObject, instanceListeners, scheduler );
    when( osgiPluginTracker.findOrCreateBeanFactoryFor( serviceObject ) ).thenReturn( null );
    delayedInstanceNotifier.run();
    verify( scheduler ).schedule( delayedInstanceNotifier, 2, TimeUnit.SECONDS );
  }

  @Test
  public void testExceptionFromTracker() throws Exception {
    DelayedInstanceNotifier delayedInstanceNotifier =
        new DelayedInstanceNotifier( osgiPluginTracker, eventType, serviceObject, instanceListeners, scheduler );
    when( osgiPluginTracker.findOrCreateBeanFactoryFor( serviceObject ) )
        .thenThrow( new OSGIPluginTrackerException( "Error" ) );
    delayedInstanceNotifier.run();
    // because we caused an exception in the findOrCreateBeanFactoryFor call, it should schedule for a later try
    verify( scheduler ).schedule( delayedInstanceNotifier, 2, TimeUnit.SECONDS );
  }
}
