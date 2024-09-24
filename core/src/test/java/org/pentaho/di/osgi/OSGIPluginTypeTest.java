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
