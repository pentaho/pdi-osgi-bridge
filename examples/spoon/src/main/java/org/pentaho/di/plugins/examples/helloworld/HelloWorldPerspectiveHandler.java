package org.pentaho.di.plugins.examples.helloworld;

import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

/**
 * Event handler respond to UI events. This one responds to clicks on the menu-items added through the Plugin and
 * Perspective Overlays
 * <p/>
 * User: nbaker Date: 1/6/11
 */
public class HelloWorldPerspectiveHandler extends AbstractXulEventHandler {

  public void sayHello() throws XulException {
    XulMessageBox msg = (XulMessageBox) document.createElement( "messagebox" );
    msg.setTitle( "Hello World" );
    msg.setMessage( "Hello World. This was provided by a plugin!" );
    msg.open();
  }

  public void sayHelloToPerspective() throws XulException {
    XulMessageBox msg = (XulMessageBox) document.createElement( "messagebox" );
    msg.setTitle( "Hello World" );
    msg.setMessage( "Hello! This action is only available when the perspective is active." );
    msg.open();
  }


  public String getName() {
    return "spoonHelloExample";
  }
}
