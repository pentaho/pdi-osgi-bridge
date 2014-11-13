package org.pentaho.di.plugins.examples.texteditor;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.ui.spoon.FileListener;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPerspectiveManager;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.components.XulTab;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.w3c.dom.Node;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * For the sake of not having too many classes to look at this is both a SpoonPerspective and a FileListener. This
 * perspective uses XUL for the tabset as well as for the content of the tabs. You could reuse the tabs portion and
 * utilize plain SWT for the content if desired.
 * <p/>
 * User: nbaker Date: 1/7/11
 */
public class EditorPerspective extends AbstractXulPerspective implements SpoonPerspective, FileListener {

  private static EditorPerspective instance;

  Log logger = LogFactory.getLog( getClass() );

  private EditorPerspective() {
    super( "org/pentaho/di/plugins/examples/texteditor/res/perspective.xul" );
    setDefaultExtension( "nte" );
  }

  public static EditorPerspective getInstance() {
    if ( instance == null ) {
      // create it in the event thread as errors will occur otherwise

      ( (Spoon) SpoonFactory.getInstance() ).getDisplay().syncExec( new Runnable() {
        public void run() {
          instance = new EditorPerspective();
        }
      } );
    }
    return instance;
  }

  public void createNewTab() {
    EditorModel model = new EditorModel();
    XulTabAndPanel tabAndPanel = createTab( createShortName( "Untitled" ), model );

    Composite parentComposite = (Composite) tabAndPanel.panel.getManagedObject();

    EditorUI xul = new EditorUI( model );

    xul.getMainPanel().setParent( parentComposite );

    EditorMeta meta = new EditorMeta( model );
    setMetaForTab( tabAndPanel.tab, meta );
    setSelectedMeta( meta );

    parentComposite.layout( true );
  }

  @Override
  public String getDisplayName( Locale l ) {
    return "Document-based";
  }

  @Override
  public InputStream getPerspectiveIcon() {
    ClassLoader loader = getClass().getClassLoader();
    return loader.getResourceAsStream( "org/pentaho/di/plugins/examples/texteditor/res/blueprint.png" );
  }

  @Override
  public String getId() {
    return "spoonXulExample";
  }

  @Override
  public String[] getFileTypeDisplayNames( Locale locale ) {
    return new String[] { "Note" };
  }

  @Override
  public String[] getSupportedExtensions() {
    return new String[] { "nte" };
  }

  /**
   * A list of event handlers to add when the perspective is active.
   *
   * @return
   */
  @Override
  public List<XulEventHandler> getEventHandlers() {
    return null;
  }

  /**
   * A list of overlays to be added when the perspective is active. This allows you to add menu-items and enable/disable
   * UI elements when your perspective is active.
   *
   * @return
   */
  @Override
  public List<XulOverlay> getOverlays() {
    return null;
  }

  @Override
  public String getName() {
    return "Spoon Xul Example";
  }

  @Override
  public boolean onTabClose( int pos ) throws XulException {
    return false;
  }

  public boolean open( Node transNode, String fname, boolean importfile ) {

    try {
      // files may be a mix of absolute and relative. Constructing File objects to test equality
      File incomingFile = new File( fname );
      for ( Map.Entry<XulTab, EngineMetaInterface> m : this.metas.entrySet() ) {
        if ( m == null ) {
          continue;
        }
        String fileName = ( (EditorMeta) m.getValue() ).getModel().getFileName();

        if ( fileName != null && new File( fileName ).getAbsoluteFile().equals( incomingFile.getAbsoluteFile() ) ) {
          int idx = this.tabbox.getTabs().getChildNodes().indexOf( m.getKey() );
          if ( idx > -1 ) {
            SpoonPerspectiveManager.getInstance().activatePerspective( getClass() );
            this.tabbox.setSelectedIndex( idx );
            return true;
          }
        }
      }
      Spoon spoon = ( (Spoon) SpoonFactory.getInstance() );

      String content = FileUtils.readFileToString( new File( fname ), "UTF-8" );

      EditorModel model = new EditorModel();

      XulTabAndPanel tabAndPanel = createTab( createShortName( fname ), model );

      String text = content.substring( content.indexOf( "<text>" ) + 6, content.indexOf( "</text>" ) );

      EditorUI xul = new EditorUI( model );

      Composite parentComposite = (Composite) tabAndPanel.panel.getManagedObject();
      xul.getMainPanel().setParent( parentComposite );

      parentComposite.layout( true );

      model.setText( text );
      model.setFileName( fname );

      EditorMeta meta = new EditorMeta( model );
      setMetaForTab( tabAndPanel.tab, meta );
      setSelectedMeta( meta );

      File f = new File( fname );
      String fullPath = f.getAbsolutePath();
      spoon.getProperties().addLastFile( "Note", fullPath, null, false, null ); //$NON-NLS-1$
      spoon.addMenuLast();
      SpoonPerspectiveManager.getInstance().activatePerspective( getClass() );

      return true;
    } catch ( IOException e ) {
      logger.error( "Error opening file", e );
    } catch ( KettleException e ) {
      logger.error( "Error opening file", e );
    }

    return false;
  }

  public boolean save( EngineMetaInterface meta, String fname, boolean isExport ) {
    try {
      ( (EditorMeta) meta ).getModel().setFileName( fname );

      OutputStream output = null;
      try {
        File f = new File( fname );
        if ( f.exists() == false ) {
          f.createNewFile();
        }
        output = new FileOutputStream( f );
        output.write( ( "<?xml version=\"1.0\"?><text>" + ( (EditorMeta) meta ).getModel().getText() + "</text>" )
            .getBytes( "UTF-8" ) );
      } catch ( FileNotFoundException e ) {
        throw e;
      } finally {
        if ( output != null ) {
          output.close();
        }
      }
      this.tabs.getTabByIndex( this.tabbox.getSelectedIndex() ).setLabel( createShortName( fname ) );

      return true;

    } catch ( Exception e ) {
      e.printStackTrace();
    }
    return false;
  }
}
