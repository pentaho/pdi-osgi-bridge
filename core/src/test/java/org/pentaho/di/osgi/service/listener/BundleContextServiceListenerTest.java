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
