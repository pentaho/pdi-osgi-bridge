package org.pentaho.di.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.pentaho.osgi.api.IKarafFeatureWatcher;
import org.pentaho.platform.servicecoordination.api.IPhasedLifecycleEvent;
import org.pentaho.platform.servicecoordination.api.IPhasedLifecycleListener;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by nbaker on 2/19/15.
 */
public class KarafLifecycleListener implements IPhasedLifecycleListener<KettleLifecycleEvent> {
  private AtomicBoolean initialized = new AtomicBoolean( false );
  private BundleContext bundleContext;
  private IPhasedLifecycleEvent<KettleLifecycleEvent> event;
  private static KarafLifecycleListener INSTANCE = new KarafLifecycleListener();

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
          IKarafFeatureWatcher karafFeatureWatcher = bundleContext.getService( serviceReference );
          try {
            karafFeatureWatcher.waitForFeatures();
          } catch ( IKarafFeatureWatcher.FeatureWatcherException e ) {
            event.exception( e );
          }
          event.accept();
        }
      } );
      thread.setDaemon( true );
      thread.start();

    }
  }

  public void setBundleContext( BundleContext bundleContext ){
    this.bundleContext = bundleContext;

    maybeStartFeatureWatcher();

  }
}
