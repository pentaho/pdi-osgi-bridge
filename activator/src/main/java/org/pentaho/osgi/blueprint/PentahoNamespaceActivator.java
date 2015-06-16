package org.pentaho.osgi.blueprint;

import org.apache.aries.blueprint.NamespaceHandler;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Hashtable;

/**
 * An Activator which registers the Pentaho Blueprint NamespaceHandler
 * <p/>
 * Created by nbaker on 2/11/15.
 */
public class PentahoNamespaceActivator implements BundleActivator {
  @Override public void start( BundleContext bundleContext ) throws Exception {

    bundleContext.registerService( NamespaceHandler.class, new PentahoNamespaceHandler( bundleContext ),
        new Hashtable<String, String>() {{
          put( "osgi.service.blueprint.namespace",
              "http://www.pentaho.com/xml/schemas/pentaho-blueprint" );
        }}
    );
  }

  @Override public void stop( BundleContext bundleContext ) throws Exception {

  }
}
