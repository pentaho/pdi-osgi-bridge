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
