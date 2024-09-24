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

package org.pentaho.osgi.bridge.activator;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.startlevel.FrameworkStartLevel;
import org.pentaho.di.core.util.ExecutorUtil;

import java.util.Hashtable;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.*;

public class BridgeActivatorTest {

  BridgeActivator bridgeActivator;
  BundleContext bundleContext;

  @Before
  public void setUp() throws Exception {
    bundleContext = mockBundleContext( 100 );
    Filter mockFilter = mock( Filter.class );
    when( bundleContext.createFilter( anyString() )).thenReturn( mockFilter );
    bridgeActivator = new BridgeActivator();
  }

  @Test
  public void testStart() throws Exception {
    bridgeActivator.start( bundleContext );
    verify( bundleContext, atLeastOnce() ).createFilter( anyString() );
    verify( bundleContext, times( 8 ) ).addServiceListener( any(), anyString() );
    verify( bundleContext, atLeastOnce() ).registerService( ExecutorService.class,
        ExecutorUtil.getExecutor(), new Hashtable<String, Object>() );
  }

  @Test
  public void testStop() throws Exception {
    bridgeActivator.start( bundleContext );
    bridgeActivator.stop( bundleContext );
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