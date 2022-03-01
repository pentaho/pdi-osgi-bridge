/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import static org.mockito.Matchers.anyString;
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
    verify( bundleContext, times( 8 ) ).addServiceListener( (ServiceListener) anyObject(), anyString() );
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