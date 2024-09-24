/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *  *******************************************************************************
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 *
 */

package org.pentaho.di.osgi.service.tracker;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.osgi.BlueprintBeanFactory;
import org.pentaho.di.osgi.OSGIPlugin;
import org.pentaho.di.osgi.PdiPluginSupplementalClassMappings;

/**
 * This OSGI ServiceTracker watches for Supplemental PDI Plugin mappings which need to be added to the already created
 * OSGI Plugin.
 *
 * This mechanism is no longer needed now that PluginRegistry.addClassFactory has been added.
 *
 * Created by nbaker on 3/2/17.
 */
public class PdiPluginSupplementalClassMappingsTracker
  extends ServiceTracker<PdiPluginSupplementalClassMappings, PdiPluginSupplementalClassMappings> {

  private OSGIPlugin osgiPlugin;

  public PdiPluginSupplementalClassMappingsTracker( BundleContext bundleContext,
                                                    Class<? extends PluginTypeInterface> pluginTypeFromPlugin,
                                                    OSGIPlugin osgiPlugin ) throws InvalidSyntaxException {
    super( bundleContext, bundleContext.createFilter(
      "(&(objectClass=" + PdiPluginSupplementalClassMappings.class.getName() + ")&(id=" + osgiPlugin.getID()
        + "))" ), null );
    this.osgiPlugin = osgiPlugin;
  }

  @Override public PdiPluginSupplementalClassMappings addingService(
    ServiceReference<PdiPluginSupplementalClassMappings> reference ) {

    PdiPluginSupplementalClassMappings pdiPluginSupplementalClassMappings = super.addingService( reference );

    pdiPluginSupplementalClassMappings.getClassToBeanNameMap().forEach( ( aClass, s ) -> osgiPlugin.addClassFactory( aClass, new BlueprintBeanFactory( s, pdiPluginSupplementalClassMappings.getContainer() ) ) );
    return pdiPluginSupplementalClassMappings;
  }
}
