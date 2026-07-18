/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.osgi.service.tracker;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.pentaho.di.osgi.OSGIPluginTracker;
import org.pentaho.osgi.api.ProxyUnwrapper;

/**
 * User: RFellows Date: 2/19/15
 */
public class ProxyUnwrapperServiceTracker extends ServiceTracker {
  private final OSGIPluginTracker osgiPluginTracker;

  public ProxyUnwrapperServiceTracker( BundleContext context, OSGIPluginTracker osgiPluginTracker ) {
    super( context, ProxyUnwrapper.class.getName(), null );
    this.osgiPluginTracker = osgiPluginTracker;
  }

  @Override
  public Object addingService( ServiceReference reference ) {
    osgiPluginTracker.setProxyUnwrapper( (ProxyUnwrapper) context.getService( reference ) );
    return reference;
  }
}
