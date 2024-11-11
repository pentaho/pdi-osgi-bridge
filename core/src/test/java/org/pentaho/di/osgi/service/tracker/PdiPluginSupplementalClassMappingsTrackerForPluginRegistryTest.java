/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.osgi.service.tracker;

import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.pentaho.di.core.logging.LoggingPluginType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.osgi.PdiPluginSupplementalClassMappings;

import java.util.Collections;

import static org.mockito.Mockito.*;

/**
 * Test that the tracker registers extra mappings with the PluginRegistry.
 * <p>
 * Created by nbaker on 3/19/17.
 */
public class PdiPluginSupplementalClassMappingsTrackerForPluginRegistryTest {

  @Test
  public void addingService() throws Exception {

    PluginRegistry registry = mock( PluginRegistry.class );
    BundleContext bundleContext = mock(
      BundleContext.class );
    PdiPluginSupplementalClassMappingsTrackerForPluginRegistry tracker =
      new PdiPluginSupplementalClassMappingsTrackerForPluginRegistry(
        bundleContext );
    tracker.setPluginRegistry( registry );

    ServiceReference<PdiPluginSupplementalClassMappings> mappingsServiceReference = mock( ServiceReference.class );
    when( mappingsServiceReference.getProperty( "type" ) ).thenReturn( LoggingPluginType.class );
    when( mappingsServiceReference.getProperty( "id" ) ).thenReturn( "plugin-id" );
    PdiPluginSupplementalClassMappings mappings = mock( PdiPluginSupplementalClassMappings.class );
    when( bundleContext.getService( mappingsServiceReference ) ).thenReturn( mappings );
    when( mappings.getClassToBeanNameMap() ).thenReturn( Collections.singletonMap( String.class, "FooID" ) );
    when( mappings.getContainer() ).thenReturn( mock( BlueprintContainer.class ) );
    tracker.addingService( mappingsServiceReference );


    verify( registry, times( 1 ) )
      .addClassFactory( eq( LoggingPluginType.class ), eq( String.class ), eq( "plugin-id" ), any() );

  }

}