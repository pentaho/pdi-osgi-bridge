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


package org.pentaho.di.osgi;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bryan on 8/15/14.
 */
public class BundleClassloaderWrapperTest {
  private Bundle bundle;
  private ClassLoader parent;

  @Before
  public void setup() {
    bundle = mock( Bundle.class );
    parent = mock( ClassLoader.class );
  }

  @Test
  public void testBundleConstructor() {
    assertEquals( bundle, new BundleClassloaderWrapper( bundle ).getBundle() );
  }

  @Test
  public void testBundleAndParentConstructor() {
    BundleClassloaderWrapper bundleClassloaderWrapper = new BundleClassloaderWrapper( bundle, parent );
    assertEquals( bundle, bundleClassloaderWrapper.getBundle() );
    assertEquals( parent, bundleClassloaderWrapper.getParent() );
  }

  @Test
  public void testFindResources() throws IOException {
    Enumeration result = mock( Enumeration.class );
    String resource = "RESOURCE";
    when( bundle.getResources( resource ) ).thenReturn( result );
    BundleClassloaderWrapper bundleClassloaderWrapper = new BundleClassloaderWrapper( bundle );
    assertEquals( result, bundleClassloaderWrapper.findResources( resource ) );
  }

  @Test
  public void testFindResource() throws MalformedURLException {
    URL result = new URL( "http://www.penaho.com" );
    String resource = "RESOURCE";
    when( bundle.getResource( resource ) ).thenReturn( result );
    BundleClassloaderWrapper bundleClassloaderWrapper = new BundleClassloaderWrapper( bundle );
    assertEquals( result, bundleClassloaderWrapper.findResource( resource ) );
  }

  @Test
  public void testFindClass() throws ClassNotFoundException {
    Class result = Map.class;
    String name = "RESOURCE";
    when( bundle.loadClass( name ) ).thenReturn( result );
    BundleClassloaderWrapper bundleClassloaderWrapper = new BundleClassloaderWrapper( bundle );
    assertEquals( result, bundleClassloaderWrapper.loadClass( name ) );
  }

  @Test
  public void testGetResourceNoParent() throws MalformedURLException {
    URL result = new URL( "http://www.penaho.com" );
    String resource = "RESOURCE";
    when( bundle.getResource( resource ) ).thenReturn( result );
    BundleClassloaderWrapper bundleClassloaderWrapper = new BundleClassloaderWrapper( bundle );
    assertEquals( result, bundleClassloaderWrapper.getResource( resource ) );
  }

  @Test
  public void testGetResourceParent() throws MalformedURLException {
    URL result = new URL( "http://www.penaho.com" );
    String resource = "RESOURCE";
    when( bundle.getResource( resource ) ).thenReturn( result );
    BundleClassloaderWrapper bundleClassloaderWrapper = new BundleClassloaderWrapper( bundle, parent );
    assertEquals( result, bundleClassloaderWrapper.getResource( resource ) );
  }

  @Test
  public void testLoadClassNoParentNoResolve() throws ClassNotFoundException {
    Class result = Map.class;
    String name = "RESOURCE";
    when( bundle.loadClass( name ) ).thenReturn( result );
    BundleClassloaderWrapper bundleClassloaderWrapper = new BundleClassloaderWrapper( bundle );
    assertEquals( result, bundleClassloaderWrapper.loadClass( name, false ) );
  }

  @Test
  public void testLoadClassNoParentResolve() throws ClassNotFoundException {
    Class result = Map.class;
    String name = "RESOURCE";
    when( bundle.loadClass( name ) ).thenReturn( result );
    BundleClassloaderWrapper bundleClassloaderWrapper = new BundleClassloaderWrapper( bundle );
    assertEquals( result, bundleClassloaderWrapper.loadClass( name, true ) );
  }

  @Test
  public void testLoadClassParentNoResolve() throws ClassNotFoundException {
    Class result = Map.class;
    String name = "RESOURCE";
    when( bundle.loadClass( name ) ).thenReturn( result );
    BundleClassloaderWrapper bundleClassloaderWrapper = new BundleClassloaderWrapper( bundle, parent );
    assertEquals( result, bundleClassloaderWrapper.loadClass( name, false ) );
  }

  @Test
  public void testLoadClassParentResolve() throws ClassNotFoundException {
    Class result = Map.class;
    String name = "RESOURCE";
    when( bundle.loadClass( name ) ).thenReturn( result );
    BundleClassloaderWrapper bundleClassloaderWrapper = new BundleClassloaderWrapper( bundle, parent );
    assertEquals( result, bundleClassloaderWrapper.loadClass( name, true ) );
  }
}
