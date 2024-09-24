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

import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.DefaultBinding;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;


/**
 * User: nbaker Date: 1/7/11
 */
public class EditorController extends AbstractXulEventHandler {
  EditorModel model;

  public EditorController() {
    model = new EditorModel();
  }

  public EditorController( EditorModel model ) {
    this.model = model;
  }

  public void init() {
    XulTextbox textbox = (XulTextbox) document.getElementById( "notepad" );
    Binding bind = new DefaultBinding( textbox, "value", model, "text" );
    bind.setBindingType( Binding.Type.BI_DIRECTIONAL );
    document.addBinding( bind );
  }

  public EditorModel getModel() {
    return model;
  }

  public String getName() {
    return "handler";
  }
}
