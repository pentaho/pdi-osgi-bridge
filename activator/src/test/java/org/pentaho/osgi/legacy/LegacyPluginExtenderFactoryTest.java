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

package org.pentaho.osgi.legacy;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LegacyPluginExtenderFactoryTest {

  private static final String TEST_CLASS_NAME = "TestClassName";
  private static final String PLUGIN_TYPE = "testPluginType";
  private static final String PLUGIN_ID = "testPluginId";

  private static PluginInterface plugin;
  private static String[] ids = { PLUGIN_ID };

  private LegacyPluginExtenderFactory legacyPluginExtenderFactory;
  private BundleContext bundleContext;
  private Bundle bundle;
  private BundleWiring bundleWiring;

  @BeforeClass
  public static void setUpClass() throws Exception {
    plugin = mock( PluginInterface.class );
    when( plugin.getIds() ).thenReturn( ids );
    when( plugin.matches( anyString() )).thenReturn( Boolean.TRUE );

    PluginRegistry pluginRegistry = PluginRegistry.getInstance();
    pluginRegistry.registerPluginType( MockPluginType.class );
    pluginRegistry.registerPlugin( MockPluginType.class, plugin  );
  }

  @Before
  public void setUp() throws Exception {
    bundleContext = mock( BundleContext.class );
    bundle = mock( Bundle.class );
    bundleWiring = mock( BundleWiring.class );
    List<URL> urls = new ArrayList<URL>();
    when( bundleWiring.findEntries( anyString(), anyString(), anyInt() ) ).thenReturn( urls );
    when( bundleContext.getBundle() ).thenReturn( bundle );
    when( bundle.adapt( BundleWiring.class )).thenReturn( bundleWiring );
  }

  @Test
  public void testCreate() throws Exception {
    legacyPluginExtenderFactory = new LegacyPluginExtenderFactory( bundleContext, MockPluginType.class.getCanonicalName(), PLUGIN_ID );

    MockPluginType result = (MockPluginType) legacyPluginExtenderFactory.create( MockPluginType.class.getName() );
    assertNotNull( result );
    assert( result.getClass().isAssignableFrom( MockPluginType.class ));
  }

  @Test
  public void testCreate1() throws Exception {
    List argumentsList = new ArrayList();
    argumentsList.add( PLUGIN_ID );

    legacyPluginExtenderFactory = new LegacyPluginExtenderFactory( bundleContext, MockPluginType.class.getCanonicalName(), PLUGIN_ID );

    MockPluginType result = (MockPluginType) legacyPluginExtenderFactory.create( MockPluginType.class.getName(), argumentsList );
    assertNotNull( result );
    assert( result.getClass().isAssignableFrom( MockPluginType.class ));
  }

  // Test passing an invalid constructor argument to MockPlugin, expected exception
  @Test(expected=InstantiationException.class)
  public void testCreateInvalidArgument() throws Exception {
    List argumentsList = new ArrayList();
    argumentsList.add( new Integer( 7 ) );

    legacyPluginExtenderFactory = new LegacyPluginExtenderFactory( bundleContext, MockPluginType.class.getCanonicalName(), PLUGIN_ID );

    MockPluginType result = (MockPluginType) legacyPluginExtenderFactory.create( MockPluginType.class.getName(), argumentsList );
  }

}