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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.pentaho.di.osgi.OSGIActivator;
import org.pentaho.osgi.blueprint.PentahoNamespaceActivator;
import org.pentaho.osgi.bridge.KarafCapabilityProvider;

/**
 * The "Main" Activator for this bundle. Bundles can only have one Activator so this one chains to others as needed.
 * Created by nbaker on 2/13/15.
 */
public class BridgeActivator implements BundleActivator {
  private KarafCapabilityProvider karafCapabilityProvider;
  private OSGIActivator osgiActivator;

  @Override public void start( BundleContext bundleContext ) throws Exception {

    this.karafCapabilityProvider = new KarafCapabilityProvider( bundleContext );
    this.karafCapabilityProvider.open();

    this.osgiActivator = new OSGIActivator();
    this.osgiActivator.start( bundleContext );
    new PentahoNamespaceActivator().start( bundleContext );
  }

  @Override public void stop( BundleContext bundleContext ) throws Exception {
    this.osgiActivator.stop( null );
    this.karafCapabilityProvider.close();
  }
}
