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
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.osgi.BlueprintBeanFactory;
import org.pentaho.di.osgi.PdiPluginSupplementalClassMappings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This tracker watches for PdiPluginSupplementalClassMappings registrations and adds them to the PluginRegistry.
 * <p>
 * Created by nbaker on 3/17/17.
 */
public class PdiPluginSupplementalClassMappingsTrackerForPluginRegistry
  extends ServiceTracker<PdiPluginSupplementalClassMappings, PdiPluginSupplementalClassMappings> {

  private Logger logger = LoggerFactory.getLogger( getClass() );
  private PluginRegistry registry;

  public PdiPluginSupplementalClassMappingsTrackerForPluginRegistry( BundleContext bundleContext )
    throws InvalidSyntaxException {
    super( bundleContext, PdiPluginSupplementalClassMappings.class, null );
  }

  @Override public PdiPluginSupplementalClassMappings addingService(
    ServiceReference<PdiPluginSupplementalClassMappings> reference ) {

    PdiPluginSupplementalClassMappings pdiPluginSupplementalClassMappings = super.addingService( reference );

    // Loop thru and add plugin mappings to PluginRegistry.
    pdiPluginSupplementalClassMappings.getClassToBeanNameMap().forEach( ( Class aClass, String s ) -> {
      BlueprintBeanFactory blueprintBeanFactory =
        new BlueprintBeanFactory( s, pdiPluginSupplementalClassMappings.getContainer() );
      try {
        getPluginRegistry()
          .addClassFactory( (Class) reference.getProperty( "type" ), aClass, (String) reference.getProperty( "id" ),
            () -> blueprintBeanFactory.create( aClass ) );
      } catch ( KettlePluginException e ) {
        logger.error( "Unexpected error registering supplemental plugin mapping", e );
      }
    } );

    return pdiPluginSupplementalClassMappings;
  }

  private PluginRegistry getPluginRegistry() {
    return registry != null ? registry : PluginRegistry.getInstance();
  }

  protected void setPluginRegistry( PluginRegistry registry ) {
    this.registry = registry;
  }

  @Override public void removedService( ServiceReference<PdiPluginSupplementalClassMappings> reference,
                                        PdiPluginSupplementalClassMappings service ) {


    // TODO: Remove the already registered factory.
    super.removedService( reference, service );
  }
}
