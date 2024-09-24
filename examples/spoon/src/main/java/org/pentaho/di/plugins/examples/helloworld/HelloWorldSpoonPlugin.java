package org.pentaho.di.plugins.examples.helloworld;

import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonLifecycleListener;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPerspectiveManager;
import org.pentaho.di.ui.spoon.SpoonPlugin;
import org.pentaho.di.ui.spoon.SpoonPluginCategories;
import org.pentaho.di.ui.spoon.SpoonPluginInterface;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;

/**
 * User: nbaker Date: 1/6/11
 */

@SpoonPlugin( id = "SpoonExample", image = "" )
@SpoonPluginCategories( { "spoon" } )
public class HelloWorldSpoonPlugin implements SpoonPluginInterface {

  private XulDomContainer container;
  private SpoonPerspective perspective;

  public HelloWorldSpoonPlugin() {
  }

  /**
   * This call tells the Spoon Plugin to make it's modification to the particular area in Spoon (category). The current
   * possible areas are: trans-graph, job-graph, database_dialog and spoon.
   *
   * @param category  Area to modify
   * @param container The XUL-document for the particular category.
   * @throws XulException
   */
  public void applyToContainer( String category, XulDomContainer container ) throws XulException {
    this.container = container;
    container.registerClassLoader( getClass().getClassLoader() );
    if ( category.equals( "spoon" ) ) {
      container.loadOverlay( "org/pentaho/di/plugins/examples/helloworld/res/spoon_overlay.xul" );
      container.addEventHandler( new HelloWorldPerspectiveHandler() );
    }
  }

  public SpoonLifecycleListener getLifecycleListener() {
    return null;
  }

  // May be called more than once, don't construct your perspective here.
  public SpoonPerspective getPerspective() {
    return perspective;
  }

  // May be called more than once, don't construct your perspective here.
  //  public SpoonPerspective getPerspective() {
  //    return HelloWorldSwtPerspective.getInstance();
  //  }

  public void setPerspective( SpoonPerspective perspective ) {
    this.perspective = perspective;
  }

  public void removeFromContainer() throws XulException {
    if ( container == null ) {
      return;
    }
    ( (Spoon) SpoonFactory.getInstance() ).getDisplay().syncExec( new Runnable() {
      public void run() {
        try {
          container.removeOverlay( "org/pentaho/di/plugins/examples/helloworld/res/spoon_overlay.xul" );
        } catch ( XulException e ) {
          e.printStackTrace();
        }
        SpoonPerspectiveManager.getInstance().removePerspective( HelloWorldSwtPerspective.getInstance() );
        container.deRegisterClassLoader( HelloWorldSpoonPlugin.class.getClassLoader() );
      }
    } );
  }
}

