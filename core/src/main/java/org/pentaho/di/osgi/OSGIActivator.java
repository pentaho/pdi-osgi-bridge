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

package org.pentaho.di.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.osgi.service.tracker.BeanFactoryLookupServiceTracker;
import org.pentaho.di.osgi.service.tracker.PdiPluginSupplementalClassMappingsTrackerForPluginRegistry;
import org.pentaho.di.osgi.service.tracker.ProxyUnwrapperServiceTracker;
import org.pentaho.osgi.api.BeanFactory;

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
    this.bundleContext = bundleContext;
    proxyUnwrapperServiceTracker = new ProxyUnwrapperServiceTracker( bundleContext, osgiPluginTracker );
    proxyUnwrapperServiceTracker.open();
    osgiPluginTracker.setBundleContext( bundleContext );
    osgiPluginTracker.registerPluginClass( BeanFactory.class );
    osgiPluginTracker.registerPluginClass( PluginInterface.class );
    beanFactoryLookupServiceTracker = new BeanFactoryLookupServiceTracker( bundleContext, osgiPluginTracker );
    beanFactoryLookupServiceTracker.open();
    pdiPluginSupplementalClassMappingsTrackerForPluginRegistry =
      new PdiPluginSupplementalClassMappingsTrackerForPluginRegistry( bundleContext );
    pdiPluginSupplementalClassMappingsTrackerForPluginRegistry.open();

    // Make sure all activation is done BEFORE this call. It will block until all bundles are registered
    KarafLifecycleListener.getInstance().setBundleContext( bundleContext );
  }

  public void stop( BundleContext bundleContext ) throws Exception {
    KarafLifecycleListener.getInstance().setBundleContext( null );
    osgiPluginTracker.shutdown();
    beanFactoryLookupServiceTracker.close();
    proxyUnwrapperServiceTracker.close();
    pdiPluginSupplementalClassMappingsTrackerForPluginRegistry.close();
  }
}
