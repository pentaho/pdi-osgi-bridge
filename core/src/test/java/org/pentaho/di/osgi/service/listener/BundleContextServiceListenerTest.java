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


package org.pentaho.di.osgi.service.listener;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.pentaho.di.osgi.service.lifecycle.LifecycleEvent;
import org.pentaho.di.osgi.service.notifier.DelayedInstanceNotifier;
import org.pentaho.di.osgi.service.notifier.DelayedInstanceNotifierFactory;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * Created by bryan on 8/18/14.
 */
public class BundleContextServiceListenerTest {
  private Map<ServiceReference, Object> referenceToInstanceMap;
  private DelayedInstanceNotifierFactory delayedInstanceNotifierFactory;
  private BundleContextServiceListener bundleContextServiceListener;
  private ServiceReference serviceReference;

  @Before
  public void setup() {
    referenceToInstanceMap = new HashMap<ServiceReference, Object>();
    delayedInstanceNotifierFactory = mock( DelayedInstanceNotifierFactory.class );
    bundleContextServiceListener =
      new BundleContextServiceListener( referenceToInstanceMap, delayedInstanceNotifierFactory );
    serviceReference = mock( ServiceReference.class );
  }

  @Test
  public void testReferenceToInstanceMapDoesntContainKey() {
    bundleContextServiceListener.serviceChanged( new ServiceEvent( ServiceEvent.MODIFIED, serviceReference ) );
    verifyNoMoreInteractions( delayedInstanceNotifierFactory );
  }

  @Test
  public void testModified() {
    Object instance = mock( Object.class );
    referenceToInstanceMap.put( serviceReference, instance );
    DelayedInstanceNotifier delayedInstanceNotifier = mock( DelayedInstanceNotifier.class );
    when( delayedInstanceNotifierFactory.create( instance, LifecycleEvent.MODIFY ) )
      .thenReturn( delayedInstanceNotifier );
    bundleContextServiceListener.serviceChanged( new ServiceEvent( ServiceEvent.MODIFIED, serviceReference ) );
    verify( delayedInstanceNotifier ).run();
  }

  @Test
  public void testUnregistering() {
    Object instance = mock( Object.class );
    referenceToInstanceMap.put( serviceReference, instance );
    DelayedInstanceNotifier delayedInstanceNotifier = mock( DelayedInstanceNotifier.class );
    when( delayedInstanceNotifierFactory.create( instance, LifecycleEvent.STOP ) )
      .thenReturn( delayedInstanceNotifier );
    bundleContextServiceListener.serviceChanged( new ServiceEvent( ServiceEvent.UNREGISTERING, serviceReference ) );
    verify( delayedInstanceNotifier ).run();
  }

  @Test
  public void testOther() {
    Object instance = mock( Object.class );
    referenceToInstanceMap.put( serviceReference, instance );
    DelayedInstanceNotifier delayedInstanceNotifier = mock( DelayedInstanceNotifier.class );
    when( delayedInstanceNotifierFactory.create( instance, LifecycleEvent.MODIFY ) )
      .thenReturn( delayedInstanceNotifier );
    bundleContextServiceListener.serviceChanged( new ServiceEvent( ServiceEvent.REGISTERED, serviceReference ) );
    verify( delayedInstanceNotifier ).run();
  }
}
