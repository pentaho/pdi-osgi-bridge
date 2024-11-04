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
