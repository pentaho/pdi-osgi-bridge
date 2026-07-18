/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.osgi.service.notifier;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by bryan on 8/18/14.
 */
public class DelayedInstanceNotifierFactoryTest {
  @Test
  public void testCreate() {
    assertNotNull( new DelayedInstanceNotifierFactory( null, null, null ).create( null, null ) );
  }
}
