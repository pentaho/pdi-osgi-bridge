/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

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
