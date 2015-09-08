package org.pentaho.di.osgi;

import org.pentaho.di.core.lifecycle.KettleLifecycleListener;
import org.pentaho.di.core.lifecycle.LifecycleException;

/**
 * Created by nbaker on 2/18/15.
 */
public class KettleLifeCycleAdapter implements KettleLifecycleListener {

  private KettlePhaseLifecycleManager manager;

  public KettleLifeCycleAdapter() {
    this.manager = KettlePhaseLifecycleManager.getInstance();
    manager.addLifecycleListener( KarafLifecycleListener.getInstance() );
  }

  @Override public void onEnvironmentInit() throws LifecycleException {
    try {
      manager.setPhaseAndWait( 1 );
    } catch ( InterruptedException e ) {
      throw new LifecycleException( e, true );
    }
  }

  @Override public void onEnvironmentShutdown() {
    try {
      manager.setPhaseAndWait( 2 );
    } catch ( InterruptedException e ) {
      throw new IllegalStateException( "Error shutting down", e );
    }
  }
}

