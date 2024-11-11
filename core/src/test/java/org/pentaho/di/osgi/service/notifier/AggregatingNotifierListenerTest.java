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
