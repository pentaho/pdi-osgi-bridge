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
import org.osgi.framework.BundleContext;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.osgi.api.BeanFactory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by bryan on 8/15/14.
 */
public class OSGIActivatorTest {
  private OSGIPluginTracker osgiPluginTracker;
  private BundleContext bundleContext;
  private OSGIActivator osgiActivator;

  @Before
  public void setup() {
    osgiPluginTracker = mock( OSGIPluginTracker.class );
    bundleContext = mock( BundleContext.class );
    osgiActivator = new OSGIActivator();
    osgiActivator.setBundleContext( bundleContext );
    osgiActivator.setOsgiPluginTracker( osgiPluginTracker );
  }

  @Test
  public void testGetBundleContext() {
    assertEquals( bundleContext, osgiActivator.getBundleContext() );
  }

  @Test
  public void testStart() throws Exception {
    osgiActivator.start();
    verify( osgiPluginTracker ).setBundleContext( bundleContext );
    verify( osgiPluginTracker ).registerPluginClass( BeanFactory.class );
    verify( osgiPluginTracker ).registerPluginClass( PluginInterface.class );
  }

  @Test
  public void testStop() throws Exception {
    osgiActivator.stop( bundleContext );
    verify( osgiPluginTracker ).shutdown();
  }
}
