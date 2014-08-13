package org.pentaho.di.osgi;

import org.pentaho.di.core.annotations.KettleLifecyclePlugin;
import org.pentaho.di.core.lifecycle.KettleLifecycleListener;
import org.pentaho.di.core.lifecycle.LifecycleException;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by bryan on 8/13/14.
 */
@KettleLifecyclePlugin( id = "OSGIKettleLifecyclePlugin", name = "OSGIKettleLifecyclePlugin" )
public class OSGIKettleLifecycleListener implements KettleLifecycleListener {
  private static final AtomicBoolean doneInitializing = new AtomicBoolean( false );

  public static void setDoneInitializing() {
    doneInitializing.set( true );
  }

  @Override public void onEnvironmentInit() throws LifecycleException {
    while ( !doneInitializing.get() ) {
      try {
        Thread.sleep( 100 );
      } catch ( InterruptedException e ) {
        e.printStackTrace();
      }
    }
  }

  @Override public void onEnvironmentShutdown() {

  }
}
