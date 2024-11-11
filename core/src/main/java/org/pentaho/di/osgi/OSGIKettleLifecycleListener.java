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

import org.pentaho.di.core.lifecycle.KettleLifecycleListener;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.osgi.registryExtension.OSGIPluginRegistryExtension;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by bryan on 8/13/14.
 */
public class OSGIKettleLifecycleListener implements KettleLifecycleListener {
  private static final AtomicBoolean doneInitializing = new AtomicBoolean( false );

  public static void setDoneInitializing() {
    doneInitializing.set( true );
  }

  @Override public void onEnvironmentInit() throws LifecycleException {

  }

  @Override public void onEnvironmentShutdown() {
    OSGIPluginRegistryExtension.getInstance().getKarafBoot().shutdown();
  }
}
