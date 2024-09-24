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
