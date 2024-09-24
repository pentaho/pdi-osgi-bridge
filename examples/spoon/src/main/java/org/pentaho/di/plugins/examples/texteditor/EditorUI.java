package org.pentaho.di.plugins.examples.texteditor;

import org.eclipse.swt.widgets.Composite;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.swt.SwtXulLoader;

import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * XUL-based content of the Example application. Shows the usage of bindings and the basics of how to construct an MVC
 * UI.
 * <p/>
 * User: nbaker Date: 1/9/11
 */
public class EditorUI {

  private EditorModel model;
  private XulDomContainer container;

  public EditorUI( EditorModel model ) {
    this.model = model;
    try {
      SwtXulLoader loader = new SwtXulLoader();
      loader.registerClassLoader( getClass().getClassLoader() );

      container = loader.loadXul( "org/pentaho/di/plugins/examples/texteditor/res/notepad.xul",
          new PDIMessages( getClass() ) ); //$NON-NLS-1$

      EditorController controller = new EditorController( model );
      container.addEventHandler( controller );
      container.initialize();
    } catch ( XulException e ) {
      e.printStackTrace();
    }

  }

  public Composite getMainPanel() {
    if ( container == null ) {
      return null;
    }
    return (Composite) container.getDocumentRoot().getElementById( "mainPanel" ).getManagedObject();
  }


  class PDIMessages extends ResourceBundle {

    private Class clz = this.getClass();

    public PDIMessages() {
    }

    public PDIMessages( Class pkg ) {
      this.clz = pkg;
    }

    @Override
    public Enumeration<String> getKeys() {
      return null;
    }

    @Override
    protected Object handleGetObject( String key ) {
      String result = BaseMessages.getString( clz, key );
      return result;
    }

  }
}
