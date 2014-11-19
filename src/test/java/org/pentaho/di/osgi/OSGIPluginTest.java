/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
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

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.osgi.api.BeanFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bryan on 8/15/14.
 */
public class OSGIPluginTest {
  private OSGIPluginTracker osgiPluginTracker;
  private OSGIPlugin osgiPlugin;

  @Before
  public void setup() {
    osgiPluginTracker = mock( OSGIPluginTracker.class );
    osgiPlugin = new OSGIPlugin();
    osgiPlugin.setOsgiPluginTracker( osgiPluginTracker );
  }

  @Test
  public void testSetCategory() {
    String category = "CATEGORY_TEST";
    osgiPlugin.setCategory( category );
    assertEquals( category, osgiPlugin.getCategory() );
  }

  @Test
  public void testGetClassMap() {
    assertEquals( Collections.emptyMap(), osgiPlugin.getClassMap() );
  }

  @Test
  public void testSetDescription() {
    String description = "TEST_DESCRIPTION";
    osgiPlugin.setDescription( description );
    assertEquals( description, osgiPlugin.getDescription() );
  }

  @Test
  public void testSetErrorHelpFile() {
    String errorHelpFile = "TEST_ERROR_HELP_FILE";
    osgiPlugin.setErrorHelpFile( errorHelpFile );
    assertEquals( errorHelpFile, osgiPlugin.getErrorHelpFile() );
  }

  @Test
  public void testSetId() {
    String id = "TEST_ID";
    osgiPlugin.setID( id );
    assertEquals( id, osgiPlugin.getID() );
    assertEquals( 1, osgiPlugin.getIds().length );
    assertEquals( id, osgiPlugin.getIds()[ 0 ] );
  }

  @Test
  public void testSetImageFile() {
    String imageFile = "TEST_IMAGE_FILE";
    osgiPlugin.setImageFile( imageFile );
    assertEquals( imageFile, osgiPlugin.getImageFile() );
  }

  @Test
  public void testGetLibrariesEmpty() {
    assertEquals( Collections.emptyList(), osgiPlugin.getLibraries() );
  }

  @Test
  public void testSetMainType() {
    Class<Object> clazz = Object.class;
    osgiPlugin.setMainType( clazz );
    assertEquals( clazz, osgiPlugin.getMainType() );
  }

  @Test
  public void testSetName() {
    String name = "TEST_NAME";
    osgiPlugin.setName( name );
    assertEquals( name, osgiPlugin.getName() );
  }

  @Test
  public void testGetPluginDirectory() {
    assertNull( osgiPlugin.getPluginDirectory() );
  }

  @Test
  public void testSetPluginTypeInterface() {
    Class<PluginTypeInterface> clazz = PluginTypeInterface.class;
    osgiPlugin.setPluginTypeInterface( clazz );
    assertEquals( clazz, osgiPlugin.getPluginType() );
  }

  @Test
  public void testIsNativePlugin() {
    assertFalse( osgiPlugin.isNativePlugin() );
  }

  @Test
  public void testIsSeparateClassloaderNeeded() {
    assertFalse( osgiPlugin.isSeparateClassLoaderNeeded() );
  }

  @Test
  public void testMatches() {
    String id = "TEST_ID";
    osgiPlugin.setID( id );
    assertTrue( osgiPlugin.matches( id ) );
    assertFalse( osgiPlugin.matches( "OTHER_ID" ) );
  }

  @Test
  public void testSetBeanFactory() {
    BeanFactory beanFactory = mock( BeanFactory.class );
    osgiPlugin.setBeanFactory( beanFactory );
    assertEquals( beanFactory, osgiPlugin.getBeanFactory() );
  }

  @Test
  public void testSetCasesUrl() {
    String casesUrl = "CASES_URL";
    osgiPlugin.setCasesUrl( casesUrl );
    assertEquals( casesUrl, osgiPlugin.getCasesUrl() );
  }

  @Test
  public void testSetDocumentationUrl() {
    String docUrl = "DOC_URL";
    osgiPlugin.setDocumentationUrl( docUrl );
    assertEquals( docUrl, osgiPlugin.getDocumentationUrl() );
  }

  @Test
  public void teatSetForumUrl() {
    String forumUrl = "FORUM_URL";
    osgiPlugin.setForumUrl( forumUrl );
    assertEquals( forumUrl, osgiPlugin.getForumUrl() );
  }

  @Test
  public void testSetClassLoaderGroup() {
    assertNull( osgiPlugin.getClassLoaderGroup() );
    osgiPlugin.setClassLoaderGroup( "TEST" );
    assertNull( osgiPlugin.getClassLoaderGroup() );
  }

  @Test
  public void testSetClassToBeanMap() {
    Map<String, String> map = new HashMap<String, String>();
    map.put( "A", "B" );
    osgiPlugin.setClassToBeanMap( map );
    assertEquals( map, osgiPlugin.getClassToBeanMap() );
  }

  @Test
  public void testGetClassLoader() {
    ClassLoader classLoader = mock( ClassLoader.class );
    when( osgiPluginTracker.getClassLoader( osgiPlugin ) ).thenReturn( classLoader );
    assertEquals( classLoader, osgiPlugin.getClassLoader() );
  }

  @Test
  public void testLoadClassInBeanMap() throws KettlePluginException {
    Map<String, String> map = new HashMap<String, String>();
    map.put( "java.lang.Object", "list" );
    osgiPlugin.setClassToBeanMap( map );
    Object result = new Object();
    when( osgiPluginTracker.getBean( Object.class, osgiPlugin, "list" ) ).thenReturn( result );
    Object object = osgiPlugin.loadClass( Object.class );
    assertEquals( result, object );
  }

  @Test
  public void testLoadClassNotInBeanMap() throws KettlePluginException {
    ArrayList result = osgiPlugin.loadClass( ArrayList.class );
    assertNotNull( result );
    assertTrue( result instanceof ArrayList );
  }

  public void testLoadClassInstantiationException() throws KettlePluginException {
    URL url = osgiPlugin.loadClass( URL.class );
    assertNull( url );
  }
}
