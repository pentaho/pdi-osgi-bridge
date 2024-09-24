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

package org.pentaho.osgi.blueprint;

import org.apache.aries.blueprint.NamespaceHandler;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Hashtable;

/**
 * An Activator which registers the Hitachi Vantara Blueprint NamespaceHandler
 * <p/>
 * Created by nbaker on 2/11/15.
 */
public class PentahoNamespaceActivator implements BundleActivator {
  @Override public void start( BundleContext bundleContext ) throws Exception {

    bundleContext.registerService( NamespaceHandler.class, new PentahoNamespaceHandler( bundleContext ),
        new Hashtable<String, String>() {
          {
            put( "osgi.service.blueprint.namespace",
                PentahoNamespaceHandler.PENTAHO_BLUEPRINT_SCHEMA );
          }
        }
    );
  }

  @Override public void stop( BundleContext bundleContext ) throws Exception {

  }
}
