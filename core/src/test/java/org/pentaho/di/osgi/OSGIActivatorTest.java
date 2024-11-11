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


package org.pentaho.di.osgi;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.startlevel.FrameworkStartLevel;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.osgi.api.BeanFactory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    bundleContext = mockBundleContext( 100 );
    osgiActivator = new OSGIActivator();
    osgiActivator.setOsgiPluginTracker( osgiPluginTracker );
  }

  @Test
  public void testGetBundleContext() throws Exception {

    osgiActivator.start( bundleContext );
    assertEquals( bundleContext, osgiActivator.getBundleContext() );
  }

  @Test
  public void testStart() throws Exception {
    osgiActivator.start( bundleContext );
    verify( osgiPluginTracker ).setBundleContext( bundleContext );
    verify( osgiPluginTracker ).registerPluginClass( BeanFactory.class );
    verify( osgiPluginTracker ).registerPluginClass( PluginInterface.class );
  }

  @Test
  public void testStop() throws Exception {
    osgiActivator.start( bundleContext );
    osgiActivator.stop( bundleContext );
    verify( osgiPluginTracker ).shutdown();
  }

  private BundleContext mockBundleContext( int beginningStartLevel ) {
    FrameworkStartLevel frameworkStartLevel = mock( FrameworkStartLevel.class );
    when( frameworkStartLevel.getStartLevel() ).thenReturn( beginningStartLevel );

    Bundle systemBundle = mock( Bundle.class );
    when( systemBundle.adapt( eq( FrameworkStartLevel.class ) )).thenReturn( frameworkStartLevel );

    bundleContext = mock( BundleContext.class );
    when( bundleContext.getBundle( eq(0l) ) ).thenReturn( systemBundle );

    return bundleContext;
  }
}
