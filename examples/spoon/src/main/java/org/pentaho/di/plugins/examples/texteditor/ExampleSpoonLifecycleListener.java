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
package org.pentaho.di.plugins.examples.texteditor;

import org.pentaho.di.core.annotations.LifecyclePlugin;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.lifecycle.LifeEventHandler;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.lifecycle.LifecycleListener;
import org.pentaho.di.ui.spoon.Spoon;

/**
 * Used to register the FileListener for the Example application.
 * <p/>
 * User: nbaker Date: 1/10/11
 */
@LifecyclePlugin( id = "ExampleLifecycleListener" )
public class ExampleSpoonLifecycleListener implements LifecycleListener {
  public void onExit( LifeEventHandler lifeEventHandler ) throws LifecycleException {
  }

  public void onStart( LifeEventHandler lifeEventHandler ) throws LifecycleException {
    ( (Spoon) SpoonFactory.getInstance() ).addFileListener( EditorPerspective.getInstance() );
  }
}
