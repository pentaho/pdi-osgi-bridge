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

package org.pentaho.di.osgi.service.lifecycle;

import org.slf4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.BundleContext;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.osgi.OSGIPlugin;

import static org.mockito.Mockito.*;

/**
 * Created by bryan on 8/15/14.
 */
public class PluginRegistryOSGIServiceLifecycleListenerTest {
  private Logger logger;
  private PluginRegistry pluginRegistry;
  private PluginRegistryOSGIServiceLifecycleListener pluginRegistryOSGIServiceLifecycleListener;
  private OSGIPlugin plugin;

  @Before
  public void setup() {
    logger = mock( Logger.class );
    pluginRegistry = mock( PluginRegistry.class );
    BundleContext bundleContext = mock( BundleContext.class );
    pluginRegistryOSGIServiceLifecycleListener = new PluginRegistryOSGIServiceLifecycleListener( pluginRegistry );
    pluginRegistryOSGIServiceLifecycleListener.setLogger( logger );
    plugin = mock( OSGIPlugin.class );
  }

  @Test
  public void testPluginAddedNoException() throws KettlePluginException {
    final Class<? extends PluginTypeInterface> clazz = PluginTypeInterface.class;
    when( plugin.getPluginType() ).thenAnswer( new Answer<Object>() {
      @Override public Object answer( InvocationOnMock invocation ) throws Throwable {
        return clazz;
      }
    } );
    pluginRegistryOSGIServiceLifecycleListener.pluginAdded( plugin );
    verify( pluginRegistry ).registerPlugin( clazz, plugin );
  }

  @Test
  public void testPluginAddedKettlePluginException() throws KettlePluginException {
    final Class<? extends PluginTypeInterface> clazz = PluginTypeInterface.class;
    when( plugin.getPluginType() ).thenAnswer( new Answer<Object>() {
      @Override public Object answer( InvocationOnMock invocation ) throws Throwable {
        return clazz;
      }
    } );

    KettlePluginException toBeThrown = new KettlePluginException();
    doThrow( toBeThrown ).when( pluginRegistry ).registerPlugin( clazz, plugin );
    pluginRegistryOSGIServiceLifecycleListener.pluginAdded( plugin );
    verify( logger ).error( anyString(), eq( toBeThrown ) );
  }

  @Test
  public void testPluginAddedException() throws KettlePluginException {
    final Class<? extends PluginTypeInterface> clazz = PluginTypeInterface.class;
    when( plugin.getPluginType() ).thenAnswer( new Answer<Object>() {
      @Override public Object answer( InvocationOnMock invocation ) throws Throwable {
        return clazz;
      }
    } );

    RuntimeException toBeThrown = new RuntimeException();
    doThrow( toBeThrown ).when( pluginRegistry ).registerPlugin( clazz, plugin );
    pluginRegistryOSGIServiceLifecycleListener.pluginAdded( plugin );
    verify( logger ).error( anyString(), eq( toBeThrown ) );
  }

  @Test
  public void testPluginRemovedNoException() throws KettlePluginException {
    final Class<? extends PluginTypeInterface> clazz = PluginTypeInterface.class;
    when( plugin.getPluginType() ).thenAnswer( new Answer<Object>() {
      @Override public Object answer( InvocationOnMock invocation ) throws Throwable {
        return clazz;
      }
    } );
    pluginRegistryOSGIServiceLifecycleListener.pluginRemoved( plugin );
    verify( pluginRegistry ).removePlugin( clazz, plugin );
  }

  @Test
  public void testPluginRemovedException() throws KettlePluginException {
    final Class<? extends PluginTypeInterface> clazz = PluginTypeInterface.class;
    when( plugin.getPluginType() ).thenAnswer( new Answer<Object>() {
      @Override public Object answer( InvocationOnMock invocation ) throws Throwable {
        return clazz;
      }
    } );

    RuntimeException toBeThrown = new RuntimeException();
    doThrow( toBeThrown ).when( pluginRegistry ).removePlugin( clazz, plugin );
    pluginRegistryOSGIServiceLifecycleListener.pluginRemoved( plugin );
    verify( logger ).error( anyString(), eq( toBeThrown ) );
  }

  @Test
  public void testPluginChangedNoop() throws KettlePluginException {
    pluginRegistryOSGIServiceLifecycleListener.pluginChanged( plugin );
    verifyNoMoreInteractions( plugin, pluginRegistry, logger );
  }
}
