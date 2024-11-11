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


package org.pentaho.di.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.osgi.service.lifecycle.PluginRegistryOSGIServiceLifecycleListener;
import org.pentaho.di.osgi.service.tracker.BeanFactoryLookupServiceTracker;
import org.pentaho.di.osgi.service.tracker.PdiPluginSupplementalClassMappingsTrackerForPluginRegistry;
import org.pentaho.di.osgi.service.tracker.ProxyUnwrapperServiceTracker;
import org.pentaho.osgi.api.BeanFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: nbaker Date: 11/2/10
 */
public class OSGIActivator implements BundleActivator {
  private OSGIPluginTracker osgiPluginTracker;
  private BundleContext bundleContext;
  private BeanFactoryLookupServiceTracker beanFactoryLookupServiceTracker;
  private ProxyUnwrapperServiceTracker proxyUnwrapperServiceTracker;
  private PdiPluginSupplementalClassMappingsTrackerForPluginRegistry
    pdiPluginSupplementalClassMappingsTrackerForPluginRegistry;
  private Logger logger = LoggerFactory.getLogger( OSGIActivator.class );

  public OSGIActivator() {
    osgiPluginTracker = OSGIPluginTracker.getInstance();
  }

  public BundleContext getBundleContext() {
    return bundleContext;
  }


  protected void setOsgiPluginTracker( OSGIPluginTracker osgiPluginTracker ) {
    this.osgiPluginTracker = osgiPluginTracker;
  }

  @Override public void start( BundleContext bundleContext ) throws Exception {
    logger.info( "OSGIActivator started" );
    this.bundleContext = bundleContext;
    proxyUnwrapperServiceTracker = new ProxyUnwrapperServiceTracker( bundleContext, osgiPluginTracker );
    proxyUnwrapperServiceTracker.open();
    osgiPluginTracker.setBundleContext( bundleContext );
    osgiPluginTracker.registerPluginClass( BeanFactory.class );
    osgiPluginTracker.registerPluginClass( PluginInterface.class );
    beanFactoryLookupServiceTracker = new BeanFactoryLookupServiceTracker( bundleContext, osgiPluginTracker );
    beanFactoryLookupServiceTracker.open();
    osgiPluginTracker.addPluginLifecycleListener( PluginInterface.class, new PluginRegistryOSGIServiceLifecycleListener( PluginRegistry.getInstance() ) );
    pdiPluginSupplementalClassMappingsTrackerForPluginRegistry =
      new PdiPluginSupplementalClassMappingsTrackerForPluginRegistry( bundleContext );
    pdiPluginSupplementalClassMappingsTrackerForPluginRegistry.open();

    // Make sure all activation is done BEFORE this call. It will block until all bundles are registered
    KarafLifecycleListener.getInstance().setBundleContext( bundleContext );
    logger.info( "OSGIActivator start complete" );
  }

  public void stop( BundleContext bundleContext ) throws Exception {
    logger.info( "OSGIActivator stopped" );
    KarafLifecycleListener.getInstance().setBundleContext( null );
    osgiPluginTracker.shutdown();
    beanFactoryLookupServiceTracker.close();
    proxyUnwrapperServiceTracker.close();
    pdiPluginSupplementalClassMappingsTrackerForPluginRegistry.close();
    logger.info( "OSGIActivator stop complete" );
  }
}
