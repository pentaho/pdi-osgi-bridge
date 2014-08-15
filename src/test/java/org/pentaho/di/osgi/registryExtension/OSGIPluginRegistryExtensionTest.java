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

package org.pentaho.di.osgi.registryExtension;

import org.apache.commons.logging.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.karaf.KarafHost;
import org.pentaho.di.osgi.OSGIPluginTracker;

import static org.mockito.Mockito.mock;

/**
 * Created by bryan on 8/15/14.
 */
public class OSGIPluginRegistryExtensionTest {
  private OSGIPluginRegistryExtension cachedInstance;
  private OSGIPluginRegistryExtension extension;
  private OSGIPluginTracker tracker;
  private Log logger;
  private KarafHost karafHost;

  @Before
  public void setup() {
    try {
      cachedInstance = OSGIPluginRegistryExtension.getInstance();
    } catch (IllegalStateException e) {
      cachedInstance = null;
    }
    OSGIPluginRegistryExtension.setInstance( null );
    extension = new OSGIPluginRegistryExtension();
    tracker = mock( OSGIPluginTracker.class );
    extension.setTracker( tracker );
    logger = mock( Log.class );
    extension.setLogger( logger );
    karafHost = mock( KarafHost.class );
    extension.setKarafHost( karafHost );
  }

  @After
  public void tearDown() {
    OSGIPluginRegistryExtension.setInstance( cachedInstance );
  }

  @Test
  public void testInit() {
    PluginRegistry registry = mock( PluginRegistry.class );

  }
}
