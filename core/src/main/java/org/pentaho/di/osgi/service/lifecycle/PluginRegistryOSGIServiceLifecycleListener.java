/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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
import org.slf4j.LoggerFactory;
import org.osgi.framework.InvalidSyntaxException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.osgi.OSGIPlugin;
import org.pentaho.di.osgi.OSGIPluginTracker;
import org.pentaho.di.osgi.service.tracker.PdiPluginSupplementalClassMappingsTracker;

/**
 * Created by bryan on 8/15/14.
 */
public class PluginRegistryOSGIServiceLifecycleListener implements OSGIServiceLifecycleListener<PluginInterface> {
  private final PluginRegistry registry;
  private Logger logger = LoggerFactory.getLogger( getClass() );

  public PluginRegistryOSGIServiceLifecycleListener( PluginRegistry registry ) {
    this.registry = registry;
  }

  protected void setLogger( Logger logger ) {
    this.logger = logger;
  }

  @Override
  public void pluginAdded( PluginInterface serviceObject ) {
    try {
      OSGIPlugin osgiPlugin = (OSGIPlugin) serviceObject;
      Class<? extends PluginTypeInterface> pluginTypeFromPlugin = osgiPlugin.getPluginType();
      try {
        registry.registerPlugin( pluginTypeFromPlugin, serviceObject );
        openServiceTracker( pluginTypeFromPlugin, osgiPlugin);
        logger.debug( "Registered in PluginRegistry " + osgiPlugin.getID() + " " + osgiPlugin.getName() );
      } catch ( KettlePluginException e ) {
        logger.error( e.getMessage(), e );
      }
    } catch ( Exception e ) {
      logger.error( "Error notifying listener of plugin addition", e );
    }
  }

  private void openServiceTracker( Class<? extends PluginTypeInterface> pluginTypeFromPlugin, OSGIPlugin osgiPlugin ) {

    try {
      new PdiPluginSupplementalClassMappingsTracker( OSGIPluginTracker.getInstance().getBundleContext(), pluginTypeFromPlugin, osgiPlugin ).open();
      logger.debug( "PdiPluginSupplementalClassMappingsTracker started " + osgiPlugin.getID() + " " + osgiPlugin.getName() );
    } catch ( InvalidSyntaxException e ) {
      // Should never happen, this is from constructing the filter
      logger.error( "Error constructing filter for Class Mapping Tracker", e );
    }
  }

  @Override
  public void pluginRemoved( PluginInterface serviceObject ) {
    try {
      OSGIPlugin osgiPlugin = (OSGIPlugin) serviceObject;
      Class<? extends PluginTypeInterface> pluginTypeFromPlugin = osgiPlugin.getPluginType();
      registry.removePlugin( pluginTypeFromPlugin, serviceObject );
      logger.debug( "REMOVED from PluginRegistry " + osgiPlugin.getID() + " " + osgiPlugin.getName() );
    } catch ( Exception e ) {
      logger.error( "Error notifying listener of plugin removal", e );
    }
  }

  @Override
  public void pluginChanged( PluginInterface serviceObject ) {
    // No nothing
  }
}
