package org.pentaho.di.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.karaf.KarafHost;
import org.pentaho.osgi.api.BeanFactory;
import org.pentaho.osgi.api.BeanFactoryLocator;

/**
 * User: nbaker Date: 11/2/10
 */
public class OSGIActivator {

  private BundleContext bundleContext;

  public OSGIActivator() {

  }

  public OSGIActivator( BundleContext bundleContext ) {
    this.bundleContext = bundleContext;
  }

  public BundleContext getBundleContext() {
    return bundleContext;
  }

  public void setBundleContext( BundleContext bundleContext ) {
    this.bundleContext = bundleContext;
  }

  public void start() throws Exception {
    KarafHost.getInstance().setInitialized( true );
    OSGIPluginTracker.getInstance().setBundleContext( bundleContext );
    OSGIPluginTracker.getInstance().init( PluginRegistry.getInstance() );

    OSGIPluginTracker.getInstance().registerPluginClass( BeanFactory.class );
    OSGIPluginTracker.getInstance().registerPluginClass( PluginInterface.class );

    new ServiceTracker( bundleContext, BeanFactoryLocator.class.getName(), null ) {
      @Override
      public Object addingService( ServiceReference reference ) {
        OSGIPluginTracker.getInstance().setBeanFactoryLookup( (BeanFactoryLocator) context.getService( reference ) );
        return reference;
      }
    } .open();

  }

  public void stop( BundleContext bundleContext ) throws Exception {
    OSGIPluginTracker.getInstance().shutdown();
  }
}
