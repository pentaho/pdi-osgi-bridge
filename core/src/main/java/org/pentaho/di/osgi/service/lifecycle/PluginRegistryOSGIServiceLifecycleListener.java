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

package org.pentaho.di.osgi.service.lifecycle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.osgi.OSGIPlugin;

/**
 * Created by bryan on 8/15/14.
 */
public class PluginRegistryOSGIServiceLifecycleListener implements OSGIServiceLifecycleListener<PluginInterface> {
  private final PluginRegistry registry;
  private Log logger = LogFactory.getLog( getClass().getName() );

  public PluginRegistryOSGIServiceLifecycleListener( PluginRegistry registry ) {
    this.registry = registry;
  }

  protected void setLogger( Log logger ) {
    this.logger = logger;
  }

  @Override
  public void pluginAdded( PluginInterface serviceObject ) {
    try {
      OSGIPlugin osgiPlugin = (OSGIPlugin) serviceObject;
      Class<? extends PluginTypeInterface> pluginTypeFromPlugin = osgiPlugin.getPluginType();
      try {
        registry.registerPlugin( pluginTypeFromPlugin, serviceObject );
      } catch ( KettlePluginException e ) {
        logger.error( e.getMessage(), e );
      }
    } catch ( Exception e ) {
      logger.error( "Error notifying listener of plugin addition", e );
    }
  }

  @Override
  public void pluginRemoved( PluginInterface serviceObject ) {
    try {
      OSGIPlugin osgiPlugin = (OSGIPlugin) serviceObject;
      Class<? extends PluginTypeInterface> pluginTypeFromPlugin = osgiPlugin.getPluginType();
      registry.removePlugin( pluginTypeFromPlugin, serviceObject );
    } catch ( Exception e ) {
      logger.error( "Error notifying listener of plugin removal", e );
    }
  }

  @Override
  public void pluginChanged( PluginInterface serviceObject ) {
    // No nothing
  }
}
