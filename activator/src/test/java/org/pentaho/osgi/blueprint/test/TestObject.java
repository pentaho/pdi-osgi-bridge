package org.pentaho.osgi.blueprint.test;

import org.pentaho.di.core.annotations.KettleLifecyclePlugin;
import org.pentaho.di.core.lifecycle.KettleLifecycleListener;
import org.pentaho.di.core.lifecycle.LifecycleException;

/**
 * Created by nbaker on 2/11/15.
 */

@KettleLifecyclePlugin(id = "testLifeCycleListener", name = "testLifeCycleListener")
public class TestObject implements KettleLifecycleListener {
  @Override public void onEnvironmentInit() throws LifecycleException {

  }

  @Override public void onEnvironmentShutdown() {

  }
}
