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
