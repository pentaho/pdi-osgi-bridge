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
