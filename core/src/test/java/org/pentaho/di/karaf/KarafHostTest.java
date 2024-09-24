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
