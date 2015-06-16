package org.pentaho.di.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.pentaho.osgi.api.IKarafFeatureWatcher;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.objfac.OSGIObjectFactory;
import org.pentaho.platform.servicecoordination.api.IPhasedLifecycleEvent;
import org.pentaho.platform.servicecoordination.api.IPhasedLifecycleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by nbaker on 2/19/15.
 */
public class KarafLifecycleListener implements IPhasedLifecycleListener<KettleLifecycleEvent> {
  private AtomicBoolean initialized = new AtomicBoolean( false );
  private BundleContext bundleContext;
  private IPhasedLifecycleEvent<KettleLifecycleEvent> event;
  private static KarafLifecycleListener INSTANCE = new KarafLifecycleListener();
  private Logger logger = LoggerFactory.getLogger( getClass() );

  private KarafLifecycleListener( ) {

  }

  public static KarafLifecycleListener getInstance(){
    return INSTANCE;
  }

  @Override public void onPhaseChange( IPhasedLifecycleEvent<KettleLifecycleEvent> event ) {
    this.event = event;
    if( event.getNotificationObject().equals( KettleLifecycleEvent.INIT )){
      initialized.set( true );
      maybeStartFeatureWatcher();
    } else {
      // simple accept all other events
      event.accept();
    }
  }

  private void maybeStartFeatureWatcher(){
    if( bundleContext != null && initialized.get()){

      Thread thread = new Thread( new Runnable() {
        @Override public void run() {
          ServiceReference<IKarafFeatureWatcher> serviceReference =
              bundleContext.getServiceReference( IKarafFeatureWatcher.class );
          try {
            if( serviceReference == null ){
              throw new IKarafFeatureWatcher.FeatureWatcherException( "No IKarafFeatureWatcher service available");
            }
            IKarafFeatureWatcher karafFeatureWatcher = bundleContext.getService( serviceReference );
            karafFeatureWatcher.waitForFeatures();
          } catch ( IKarafFeatureWatcher.FeatureWatcherException e ) {
            logger.error( "Error in Feature Watcher", e );

            // We're not going to kill the system in the case of Feature errors, for now.
            //event.exception( e );
          }
          event.accept();
        }
      } );
      thread.setDaemon( true );
      thread.start();

    }
  }

  public void setBundleContext( BundleContext bundleContext ) {
    if ( PentahoSystem.getInitializedStatus() != PentahoSystem.SYSTEM_INITIALIZED_OK ){
      PentahoSystem.init();
      PentahoSystem.setBundleContext( bundleContext );
      PentahoSystem.registerObjectFactory( new OSGIObjectFactory( bundleContext ) );
    }

    this.bundleContext = bundleContext;

    maybeStartFeatureWatcher();

  }
}
