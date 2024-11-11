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
import org.pentaho.di.osgi.OSGIPluginTracker;
import org.pentaho.osgi.api.BeanFactoryLocator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by bryan on 8/15/14.
 */
public class BeanFactoryLookupServiceTrackerTest {
  @Test
  public void testAddingService() {
    BundleContext bundleContext = mock( BundleContext.class );
    OSGIPluginTracker osgiPluginTracker = mock( OSGIPluginTracker.class );
    BeanFactoryLookupServiceTracker tracker = new BeanFactoryLookupServiceTracker( bundleContext, osgiPluginTracker );
    ServiceReference serviceReference = mock( ServiceReference.class );
    BeanFactoryLocator service = mock( BeanFactoryLocator.class );
    when( bundleContext.getService( serviceReference ) ).thenReturn( service );
    assertEquals( serviceReference, tracker.addingService( serviceReference ) );
    verify( osgiPluginTracker ).setBeanFactoryLookup( service );
  }
}
