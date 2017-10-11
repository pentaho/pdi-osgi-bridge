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
import org.pentaho.di.osgi.service.lifecycle.LifecycleEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by bryan on 3/2/16.
 */
public class AggregatingNotifierListenerTest {

  private AggregatingNotifierListener aggregatingNotifierListener;
  private DelayedServiceNotifierListener delayedServiceNotifierListener;

  @Before
  public void setup() {
    delayedServiceNotifierListener = mock( DelayedServiceNotifierListener.class );
    aggregatingNotifierListener = new AggregatingNotifierListener();
  }

  @Test
  public void testIncrementCount() {
    assertEquals( 0, aggregatingNotifierListener.getCount() );
    aggregatingNotifierListener.incrementCount();
    assertEquals( 1, aggregatingNotifierListener.getCount() );
  }

  @Test
  public void testAddRemoveListenerAndRun() {
    LifecycleEvent event = LifecycleEvent.START;
    String serviceObject = "test";

    assertTrue( aggregatingNotifierListener.addListener( delayedServiceNotifierListener ) );
    assertFalse( aggregatingNotifierListener.addListener( delayedServiceNotifierListener ) );

    aggregatingNotifierListener.incrementCount();
    aggregatingNotifierListener.incrementCount();

    assertEquals( 2, aggregatingNotifierListener.getCount() );

    aggregatingNotifierListener.onRun( event, serviceObject );
    verify( delayedServiceNotifierListener ).onRun( event, serviceObject );

    assertEquals( 1, aggregatingNotifierListener.getCount() );

    assertTrue( aggregatingNotifierListener.removeListener( delayedServiceNotifierListener ) );
    assertFalse( aggregatingNotifierListener.removeListener( delayedServiceNotifierListener ) );

    aggregatingNotifierListener.onRun( event, serviceObject );

    assertEquals( 0, aggregatingNotifierListener.getCount() );

    // Only the first run should have happened
    verify( delayedServiceNotifierListener, times( 1 ) ).onRun( event, serviceObject );
  }
}
