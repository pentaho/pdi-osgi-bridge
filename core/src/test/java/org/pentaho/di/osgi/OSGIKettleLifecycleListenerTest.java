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

package org.pentaho.di.osgi;

import org.junit.Test;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.osgi.registryExtension.OSGIPluginRegistryExtension;

/**
 * Created by bryan on 8/15/14.
 */
public class OSGIKettleLifecycleListenerTest {
  @Test(timeout = 500L)
  public void testOnEnvironmentInit() throws LifecycleException {
    OSGIKettleLifecycleListener lifecycleListener = new OSGIKettleLifecycleListener();
    new Thread( new Runnable() {
      @Override public void run() {
        try {
          Thread.sleep( 100 );
          OSGIKettleLifecycleListener.setDoneInitializing();
        } catch ( InterruptedException e ) {
          e.printStackTrace();
        }
      }
    } ).start();
    lifecycleListener.onEnvironmentInit();
  }

  @Test
  public void testOnEnvironmentShutdownNoop() {
    new OSGIPluginRegistryExtension();
    new OSGIKettleLifecycleListener().onEnvironmentShutdown();
  }
}
