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

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.pentaho.di.osgi.OSGIPluginTracker;
import org.pentaho.osgi.api.BeanFactoryLocator;

/**
 * Created by bryan on 8/15/14.
 */
public class BeanFactoryLookupServiceTracker extends ServiceTracker {
  private final OSGIPluginTracker osgiPluginTracker;

  public BeanFactoryLookupServiceTracker( BundleContext bundleContext, OSGIPluginTracker osgiPluginTracker ) {
    super( bundleContext, BeanFactoryLocator.class.getName(), null );
    this.osgiPluginTracker = osgiPluginTracker;
  }

  @Override
  public Object addingService( ServiceReference reference ) {
    osgiPluginTracker.setBeanFactoryLookup( (BeanFactoryLocator) context.getService( reference ) );
    return reference;
  }
}
