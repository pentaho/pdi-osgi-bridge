package org.pentaho.di.plugins.examples.helloworld;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPerspectiveListener;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.impl.XulEventHandler;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by nbaker on 11/11/14.
 */
public class HelloWorldWebPerspective implements SpoonPerspective {

  private Composite comp;
  private String baseUrl;


  private void createUI() {
    comp = new Composite( ( (Spoon) SpoonFactory.getInstance() ).getShell(), SWT.BORDER );
    comp.setLayout( new GridLayout() );
    comp.setLayoutData( new GridData( GridData.FILL_BOTH ) );
    Browser browser = new Browser( comp, SWT.NONE );
    browser.setLayoutData( new GridData( GridData.FILL_BOTH ) );
    browser.setUrl( "http://localhost:8181/helloworld/index.html" );

  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public void setActive( boolean b ) {
  }

  public List<XulOverlay> getOverlays() {
    return Collections.emptyList();
  }

  public List<XulEventHandler> getEventHandlers() {
    return Collections.emptyList();
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
