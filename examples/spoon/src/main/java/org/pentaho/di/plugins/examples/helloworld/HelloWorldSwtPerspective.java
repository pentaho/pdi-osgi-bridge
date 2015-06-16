package org.pentaho.di.plugins.examples.helloworld;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPerspectiveListener;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.impl.DefaultXulOverlay;
import org.pentaho.ui.xul.impl.XulEventHandler;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * User: nbaker Date: 1/6/11
 */
public class HelloWorldSwtPerspective implements SpoonPerspective {
  private static HelloWorldSwtPerspective instance = new HelloWorldSwtPerspective();
  private Composite comp;
  private Label lbl;
  private String message;
  private MessageFormatter messageFormatter = new DefaultMessageFormatter();

  public HelloWorldSwtPerspective() {

  }

  public static HelloWorldSwtPerspective getInstance() {
    return instance;
  }

  public MessageFormatter getMessageFormatter() {
    return messageFormatter;
  }

  public void setMessageFormatter( MessageFormatter messageFormatter ) {
    this.messageFormatter = messageFormatter;
  }

  public String getMessage() {
    return messageFormatter.format( message );
  }

  public void setMessage( String message ) {
    this.message = message;
    if ( lbl != null ) {
      Display.getDefault().asyncExec( new Runnable() {
        public void run() {
          lbl.setText( getMessage() );
        }
      } );
    }
  }

  private void createUI() {
    comp = new Composite( ( (Spoon) SpoonFactory.getInstance() ).getShell(), SWT.BORDER );
    comp.setLayout( new GridLayout() );
    comp.setLayoutData( new GridData( GridData.FILL_BOTH ) );
    lbl = new Label( comp, SWT.CENTER | SWT.WRAP );
    lbl.setLayoutData( new GridData( GridData.FILL_BOTH ) );
    lbl.setText( getMessage() );
  }

  public void setActive( boolean b ) {
  }

  public List<XulOverlay> getOverlays() {
    return Collections.singletonList( (XulOverlay) new DefaultXulOverlay(
        "org/pentaho/di/plugins/examples/helloworld/res/spoon_perspective_overlay.xul" ) );
  }

  public List<XulEventHandler> getEventHandlers() {
    return Collections.singletonList( (XulEventHandler) new HelloWorldPerspectiveHandler() );
  }

  public void addPerspectiveListener( SpoonPerspectiveListener spoonPerspectiveListener ) {
  }

  public String getId() {
    return "helloWorld";
  }


  // Whatever you pass out will be reparented. Don't construct the UI in this method as it may be called more than once.
  public Composite getUI() {
    if ( comp == null ) {
      createUI();
    }
    return comp;
  }

  public String getDisplayName( Locale locale ) {
    return "Spoon Example";
  }

  public InputStream getPerspectiveIcon() {
    ClassLoader loader = getClass().getClassLoader();
    return loader.getResourceAsStream( "org/pentaho/di/plugins/examples/helloworld/res/blueprint.png" );
  }

  /**
   * This perspective is not Document based, therefore there is no EngineMeta to save/open.
   *
   * @return
   */
  public EngineMetaInterface getActiveMeta() {
    return null;
  }
}
