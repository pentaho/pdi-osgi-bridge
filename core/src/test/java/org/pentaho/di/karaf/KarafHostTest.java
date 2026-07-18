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



package org.pentaho.di.karaf;

import org.junit.Test;
import org.pentaho.di.osgi.registryExtension.OSGIPluginRegistryExtension;

import static org.junit.Assert.assertNotNull;

/**
 * Created by bryan on 8/15/14.
 */
public class KarafHostTest {
  @Test
  public void testGetInstanceNotNull() {
    new OSGIPluginRegistryExtension();
    assertNotNull( OSGIPluginRegistryExtension.getInstance().getKarafBoot() );
  }
}
