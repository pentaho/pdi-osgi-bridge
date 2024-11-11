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


package org.pentaho.di.osgi;

import org.junit.Test;
import org.pentaho.di.core.exception.KettlePluginException;

import static org.junit.Assert.*;

/**
 * Created by bryan on 8/15/14.
 */
public class OSGIPluginTypeTest {
  @Test
  public void testGetInstance() {
    assertNotNull( OSGIPluginType.getInstance() );
  }

  @Test
  public void testAddObjectTypeNoop() {
    new OSGIPluginType().addObjectType( null, null );
  }

  @Test
  public void testGetId() {
    assertEquals( OSGIPluginType.ID, new OSGIPluginType().getId() );
  }

  @Test
  public void testGetName() {
    assertEquals( OSGIPluginType.NAME, new OSGIPluginType().getName() );
  }

  @Test
  public void testGetPluginFoldersNull() {
    assertNull( new OSGIPluginType().getPluginFolders() );
  }

  @Test
  public void testHandlePluginAnnotationNoop() throws KettlePluginException {
    new OSGIPluginType().handlePluginAnnotation( null, null, null, false, null );
  }

  @Test
  public void testSearchPluginsNoop() throws KettlePluginException {
    new OSGIPluginType().searchPlugins();
  }
}
