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

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.osgi.service.lifecycle.LifecycleEvent;
import org.pentaho.di.osgi.service.lifecycle.OSGIServiceLifecycleListener;
import org.pentaho.osgi.api.BeanFactory;
import org.pentaho.osgi.api.BeanFactoryLocator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by bryan on 8/18/14.
 */
public class OSGIPluginTrackerTest {
  private OSGIPluginTracker tracker;
  private BundleContext bundleContext;

  @Before
  public void setup() {
    tracker = new OSGIPluginTracker();
    bundleContext = mock( BundleContext.class );
  }

  @Test
  public void testGetServiceObjectsEmptyList() {
    tracker.setBundleContext( bundleContext );
    Class<?> clazz = Object.class;
    Map<String, String> props = new HashMap<String, String>();
    assertEquals( Collections.emptyList(), tracker.getServiceObjects( clazz, props ) );
  }

  @Test
  public void testGetServiceObjectsList() throws InvalidSyntaxException {
    tracker.setBundleContext( bundleContext );
    Class<Object> clazz = Object.class;
    Map<String, String> props = new HashMap<String, String>();
    props.put( "test1", "test2" );
    props.put( "test3", "test4" );
    ServiceReference serviceReference = mock( ServiceReference.class );
    ServiceReference[] serviceReferences = new ServiceReference[] { serviceReference };
    when( bundleContext.getServiceReferences( eq( clazz.getName() ), anyString() ) ).thenReturn( serviceReferences );
    Object service = new Object();
    when( bundleContext.getService( serviceReference ) ).thenReturn( service );
    List<Object> services = tracker.getServiceObjects( clazz, props );
    assertEquals( 1, services.size() );
    assertEquals( service, services.get( 0 ) );
  }

  @Test
  public void testGetServiceObjectsInvalidSyntaxException() throws InvalidSyntaxException {
    Log logger = mock( Log.class );
    tracker.setLogger( logger );
    tracker.setBundleContext( bundleContext );
    Class<Object> clazz = Object.class;
    Map<String, String> props = new HashMap<String, String>();
    props.put( "test1", "test2" );
    props.put( "test3", "test4" );
    ServiceReference serviceReference = mock( ServiceReference.class );
    ServiceReference[] serviceReferences = new ServiceReference[] { serviceReference };
    String invalidSyntaxMessage = "INVALID_SYNTAX";
    InvalidSyntaxException invalidSyntaxException = new InvalidSyntaxException( invalidSyntaxMessage, null );
    when( bundleContext.getServiceReferences( eq( clazz.getName() ), anyString() ) )
      .thenThrow( invalidSyntaxException );
    tracker.getServiceObjects( clazz, props );
    verify( logger ).error( invalidSyntaxMessage, invalidSyntaxException );
  }

  @Test
  public void testGetBeanNullFactory() {
    Class<Object> clazz = Object.class;
    Object serviceClass = new Object();
    String id = "TEST_ID";
    assertNull( tracker.getBean( clazz, serviceClass, id ) );
  }

  @Test
  public void testGetBeanNotNullFactory() {
    tracker.setBundleContext( bundleContext );
    Class<Object> clazz = Object.class;
    ServiceReference serviceReference = mock( ServiceReference.class );
    BeanFactoryLocator lookup = mock( BeanFactoryLocator.class );
    BeanFactory beanFactory = mock( BeanFactory.class );
    Bundle bundle = mock( Bundle.class );
    when( serviceReference.getBundle() ).thenReturn( bundle );
    when( lookup.getBeanFactory( bundle ) ).thenReturn( beanFactory );
    tracker.setBeanFactoryLookup( lookup );
    Object instance = new Object();
    String id = "TEST_ID";
    when( bundleContext.getService( serviceReference ) ).thenReturn( instance );
    tracker.serviceChanged( clazz, LifecycleEvent.START, serviceReference );
    Object bean = new Object();
    when( beanFactory.getInstance( id, clazz ) ).thenReturn( bean );
    assertEquals( bean, tracker.getBean( clazz, instance, id ) );
  }

  @Test
  public void testGetBeanPluginPropertyNull() {
    assertNull( tracker.getBeanPluginProperty( OSGIPluginType.class, new Object(), "TEST" ) );
  }

  @Test
  public void testGetBeanPluginPropertyException() throws InvalidSyntaxException {
    Log logger = mock( Log.class );
    tracker.setLogger( logger );
    tracker.setBundleContext( bundleContext );
    Class<Object> clazz = Object.class;
    ServiceReference serviceReference = mock( ServiceReference.class );
    BeanFactoryLocator lookup = mock( BeanFactoryLocator.class );
    BeanFactory beanFactory = mock( BeanFactory.class );
    Bundle bundle = mock( Bundle.class );
    String message = "TEST_MESSAGE";
    RuntimeException runtimeException = new RuntimeException( message );
    when( bundle.getBundleContext() ).thenThrow( runtimeException );
    when( serviceReference.getBundle() ).thenReturn( bundle );
    when( lookup.getBeanFactory( bundle ) ).thenReturn( beanFactory );
    tracker.setBeanFactoryLookup( lookup );
    Object instance = new Object();
    String id = "TEST_ID";
    when( bundleContext.getService( serviceReference ) ).thenReturn( instance );
    tracker.serviceChanged( clazz, LifecycleEvent.START, serviceReference );
    Object bean = new Object();
    when( beanFactory.getInstance( id, clazz ) ).thenReturn( bean );
    ServiceReference ref = mock( ServiceReference.class );
    OSGIPlugin plugin = new OSGIPlugin();
    String ID = "PLUGIN_ID";
    plugin.setID( ID );
    tracker.getBean( clazz, instance, id );
    assertNull( tracker.getBeanPluginProperty( OSGIPluginType.class, bean, "ID" ) );
    verify( logger ).error( message, runtimeException );
  }

  @Test
  public void testGetBeanPluginPropertySuccess() throws InvalidSyntaxException {
    tracker.setBundleContext( bundleContext );
    Class<Object> clazz = Object.class;
    ServiceReference serviceReference = mock( ServiceReference.class );
    BeanFactoryLocator lookup = mock( BeanFactoryLocator.class );
    BeanFactory beanFactory = mock( BeanFactory.class );
    Bundle bundle = mock( Bundle.class );
    when( serviceReference.getBundle() ).thenReturn( bundle );
    when( lookup.getBeanFactory( bundle ) ).thenReturn( beanFactory );
    tracker.setBeanFactoryLookup( lookup );
    Object instance = new Object();
    String id = "TEST_ID";
    when( bundleContext.getService( serviceReference ) ).thenReturn( instance );
    tracker.serviceChanged( clazz, LifecycleEvent.START, serviceReference );
    Object bean = new Object();
    when( beanFactory.getInstance( id, clazz ) ).thenReturn( bean );
    BundleContext cxt = mock( BundleContext.class );
    when( bundle.getBundleContext() ).thenReturn( cxt );
    ServiceReference ref = mock( ServiceReference.class );
    when( cxt.getServiceReferences( eq( PluginInterface.class.getName() ), anyString() ) )
      .thenReturn( new ServiceReference[] { ref } );
    OSGIPlugin plugin = new OSGIPlugin();
    String ID = "PLUGIN_ID";
    plugin.setID( ID );
    when( cxt.getService( ref ) ).thenReturn( plugin );
    tracker.getBean( clazz, instance, id );
    assertEquals( ID, tracker.getBeanPluginProperty( OSGIPluginType.class, bean, "ID" ) );
  }

  @Test
  public void testGetBeanPluginPropertyNoRefs() throws InvalidSyntaxException {
    tracker.setBundleContext( bundleContext );
    Class<Object> clazz = Object.class;
    ServiceReference serviceReference = mock( ServiceReference.class );
    BeanFactoryLocator lookup = mock( BeanFactoryLocator.class );
    BeanFactory beanFactory = mock( BeanFactory.class );
    Bundle bundle = mock( Bundle.class );
    when( serviceReference.getBundle() ).thenReturn( bundle );
    when( lookup.getBeanFactory( bundle ) ).thenReturn( beanFactory );
    tracker.setBeanFactoryLookup( lookup );
    Object instance = new Object();
    String id = "TEST_ID";
    when( bundleContext.getService( serviceReference ) ).thenReturn( instance );
    tracker.serviceChanged( clazz, LifecycleEvent.START, serviceReference );
    Object bean = new Object();
    when( beanFactory.getInstance( id, clazz ) ).thenReturn( bean );
    BundleContext cxt = mock( BundleContext.class );
    when( bundle.getBundleContext() ).thenReturn( cxt );
    when( cxt.getServiceReferences( eq( PluginInterface.class.getName() ), anyString() ) )
      .thenReturn( new ServiceReference[] { } );
    OSGIPlugin plugin = new OSGIPlugin();
    String ID = "PLUGIN_ID";
    plugin.setID( ID );
    tracker.getBean( clazz, instance, id );
    assertNull( tracker.getBeanPluginProperty( OSGIPluginType.class, bean, "ID" ) );
  }

  @Test
  public void testGetBeanPluginPropertyNullRefs() throws InvalidSyntaxException {
    tracker.setBundleContext( bundleContext );
    Class<Object> clazz = Object.class;
    ServiceReference serviceReference = mock( ServiceReference.class );
    BeanFactoryLocator lookup = mock( BeanFactoryLocator.class );
    BeanFactory beanFactory = mock( BeanFactory.class );
    Bundle bundle = mock( Bundle.class );
    when( serviceReference.getBundle() ).thenReturn( bundle );
    when( lookup.getBeanFactory( bundle ) ).thenReturn( beanFactory );
    tracker.setBeanFactoryLookup( lookup );
    Object instance = new Object();
    String id = "TEST_ID";
    when( bundleContext.getService( serviceReference ) ).thenReturn( instance );
    tracker.serviceChanged( clazz, LifecycleEvent.START, serviceReference );
    Object bean = new Object();
    when( beanFactory.getInstance( id, clazz ) ).thenReturn( bean );
    BundleContext cxt = mock( BundleContext.class );
    when( bundle.getBundleContext() ).thenReturn( cxt );
    when( cxt.getServiceReferences( eq( PluginInterface.class.getName() ), anyString() ) )
      .thenReturn( null );
    OSGIPlugin plugin = new OSGIPlugin();
    String ID = "PLUGIN_ID";
    plugin.setID( ID );
    tracker.getBean( clazz, instance, id );
    assertNull( tracker.getBeanPluginProperty( OSGIPluginType.class, bean, "ID" ) );
  }

  @Test
  public void testGetClassLoaderNull() {
    assertNull( tracker.getClassLoader( new Object() ) );
  }

  @Test
  public void testGetClassLoaderSuccess() {
    tracker.setBundleContext( bundleContext );
    Class<Object> clazz = Object.class;
    ServiceReference serviceReference = mock( ServiceReference.class );
    Object instance = new Object();
    when( bundleContext.getService( serviceReference ) ).thenReturn( instance );
    Bundle bundle = mock( Bundle.class );
    when( serviceReference.getBundle() ).thenReturn( bundle );
    tracker.serviceChanged( clazz, LifecycleEvent.START, serviceReference );
    assertEquals( bundle, ( (BundleClassloaderWrapper) tracker.getClassLoader( instance ) ).getBundle() );
  }

  @Test
  public void testGetClassLoaderException() {
    Log logger = mock( Log.class );
    tracker.setLogger( logger );
    tracker.setBundleContext( bundleContext );
    String message = "EXCEPTION_MESSAGE";
    RuntimeException e = new RuntimeException( message );
    Class<Object> clazz = Object.class;
    ServiceReference serviceReference = mock( ServiceReference.class );
    Object instance = new Object();
    when( bundleContext.getService( serviceReference ) ).thenReturn( instance );
    when( serviceReference.getBundle() ).thenThrow( e );
    tracker.serviceChanged( clazz, LifecycleEvent.START, serviceReference );
    assertNull( tracker.getClassLoader( instance ) );
    verify( logger ).error( message, e );
  }

  @Test
  public void testShutdown() {
    tracker.setBundleContext( bundleContext );
    Class clazz = Map.class;
    tracker.registerPluginClass( clazz );
    tracker.shutdown();
  }

  @Test
  public void testAddPluginLifecycleListener() {
    tracker.setBundleContext( bundleContext );
    BeanFactoryLocator lookup = mock( BeanFactoryLocator.class );
    tracker.setBeanFactoryLookup( lookup );
    OSGIServiceLifecycleListener listener1 = mock( OSGIServiceLifecycleListener.class );
    tracker.addPluginLifecycleListener( Object.class, listener1 );
    OSGIServiceLifecycleListener listener2 = mock( OSGIServiceLifecycleListener.class );
    tracker.addPluginLifecycleListener( Object.class, listener2 );
    Object instance = new Object();
    Bundle bundle = mock( Bundle.class );
    ServiceReference serviceReference = mock( ServiceReference.class );
    when( serviceReference.getBundle() ).thenReturn( bundle );
    BeanFactory beanFactory = mock( BeanFactory.class );
    when( lookup.getBeanFactory( bundle ) ).thenReturn( beanFactory );
    when( bundleContext.getService( serviceReference ) ).thenReturn( instance );
    tracker.serviceChanged( Object.class, LifecycleEvent.START, serviceReference );
    verify( listener1 ).pluginAdded( instance );
    verify( listener2 ).pluginAdded( instance );
  }

  @Test
  public void testSetBundleContextWithQueue() {
    tracker.registerPluginClass( Object.class );
    assertEquals( 0, tracker.getTrackers().size() );
    tracker.setBundleContext( bundleContext );
    assertEquals( 1, tracker.getTrackers().size() );
    assertEquals( Object.class, new ArrayList<Class>( tracker.getTrackers().keySet() ).get( 0 ) );
  }

  @Test
  public void testRegisterPluginClassAlreadyTracking() {
    tracker.setBundleContext( bundleContext );
    tracker.registerPluginClass( Object.class );
    assertEquals( 1, tracker.getTrackers().size() );
    assertEquals( Object.class, new ArrayList<Class>( tracker.getTrackers().keySet() ).get( 0 ) );
    assertTrue( tracker.registerPluginClass( Object.class ) );
    assertEquals( 1, tracker.getTrackers().size() );
    assertEquals( Object.class, new ArrayList<Class>( tracker.getTrackers().keySet() ).get( 0 ) );
  }
}
