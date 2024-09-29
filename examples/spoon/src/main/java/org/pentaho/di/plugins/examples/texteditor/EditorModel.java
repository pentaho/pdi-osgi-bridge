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

import org.pentaho.ui.xul.XulEventSourceAdapter;

/**
 * This is a simple backing model for our Note application. Models are entirely optional, but encouraged.
 * <p/>
 * User: nbaker Date: 1/7/11
 */
public class EditorModel extends XulEventSourceAdapter {
  private String fileName;
  private String text;

  public String getFileName() {
    return fileName;
  }

  public void setFileName( String fName ) {
    String prevVal = this.fileName;
    this.fileName = fName;
    firePropertyChange( "fileName", prevVal, fName );
  }

  public String getText() {
    return text;
  }

  public void setText( String txt ) {
    String prevVal = this.text;
    this.text = txt;
    firePropertyChange( "text", prevVal, txt );
  }


}
