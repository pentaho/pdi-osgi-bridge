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
  @Override public void start( BundleContext bundleContext ) throws Exception {

    new KarafCapabilityProvider( bundleContext ).open();

    new OSGIActivator().start( bundleContext );
    new PentahoNamespaceActivator().start( bundleContext );
  }

  @Override public void stop( BundleContext bundleContext ) throws Exception {

  }
}
