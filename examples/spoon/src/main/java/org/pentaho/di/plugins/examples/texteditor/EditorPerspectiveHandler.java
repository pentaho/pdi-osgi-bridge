/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.di.plugins.examples.texteditor;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.ui.spoon.SpoonPerspectiveManager;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

/**
 * User: nbaker Date: 1/6/11
 */
public class EditorPerspectiveHandler extends AbstractXulEventHandler {

  public void createNew() throws KettleException {
    EditorPerspective.getInstance().createNewTab();
    SpoonPerspectiveManager.getInstance().activatePerspective( EditorPerspective.class );
  }

  public String getName() {
    return "spoonEditorExample";
  }
}
