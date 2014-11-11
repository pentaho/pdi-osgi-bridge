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
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginRegistryExtension;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.core.plugins.RegistryPlugin;
import org.pentaho.di.karaf.KarafHost;
import org.pentaho.di.osgi.OSGIPluginTracker;
import org.pentaho.di.osgi.OSGIPluginType;
import org.pentaho.di.osgi.StatusGetter;
import org.pentaho.di.osgi.service.lifecycle.PluginRegistryOSGIServiceLifecycleListener;

/**
 * Created by bryan on 8/15/14.
 */
@RegistryPlugin(id = "OSGIRegistryPlugin", name = "OSGI")
public class OSGIPluginRegistryExtension implements PluginRegistryExtension {
  private static OSGIPluginRegistryExtension INSTANCE;
  private OSGIPluginTracker tracker = OSGIPluginTracker.getInstance();
  private Log logger = LogFactory.getLog( getClass().getName() );
  private KarafHost karafHost = KarafHost.getInstance();
  private StatusGetter<Boolean> kettleClientEnvironmentInitialized = new StatusGetter<Boolean>() {
    @Override public Boolean get() {
      return KettleClientEnvironment.isInitialized();
    }
  };

  public OSGIPluginRegistryExtension() {
    INSTANCE = this;
  }

  public static OSGIPluginRegistryExtension getInstance() {
    if ( INSTANCE == null ) {
      throw new IllegalStateException( "Kettle is supposed to construct this first" );
    }
    return INSTANCE;
  }

  // FOR UNIT TEST ONLY
  protected static void setInstance( OSGIPluginRegistryExtension instance ) {
    INSTANCE = instance;
  }

  // FOR UNIT TEST ONLY
  protected void setTracker( OSGIPluginTracker tracker ) {
    this.tracker = tracker;
  }

  // FOR UNIT TEST ONLY
  protected void setLogger( Log logger ) {
    this.logger = logger;
  }

  // FOR UNIT TEST ONLY
  public void setKarafHost( KarafHost karafHost ) {
    this.karafHost = karafHost;
  }

  // FOR UNIT TEST ONLY
  protected void setKettleClientEnvironmentInitialized( StatusGetter<Boolean> kettleClientEnvironmentInitialized ) {
    this.kettleClientEnvironmentInitialized = kettleClientEnvironmentInitialized;
  }

  @Override
  public void init( final PluginRegistry registry ) {
    karafHost.init();
//    if ( kettleClientEnvironmentInitialized.get() ) {
      PluginRegistry.addPluginType( OSGIPluginType.getInstance() );
      tracker.registerPluginClass( PluginInterface.class );
      tracker.addPluginLifecycleListener( PluginInterface.class,
        new PluginRegistryOSGIServiceLifecycleListener( registry ) );
//    }
  }

  @Override
  public void searchForType( PluginTypeInterface pluginType ) {
    tracker.registerPluginClass( pluginType.getClass() );
  }

  @Override
  public String getPluginId( Class<? extends PluginTypeInterface> pluginType, Object pluginClass ) {
    try {
      return (String) tracker.getBeanPluginProperty( pluginType, pluginClass, "ID" );
    } catch ( Exception e ) {
      logger.error( e.getMessage(), e );
    }
    return null;
  }
}
