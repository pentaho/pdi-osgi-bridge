package org.pentaho.di.plugins.examples.texteditor;

/**
 * User: nbaker
 * Date: 1/7/11
 */

import org.eclipse.swt.widgets.Composite;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.spoon.FileListener;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPerspectiveListener;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.XulTab;
import org.pentaho.ui.xul.components.XulTabpanel;
import org.pentaho.ui.xul.containers.XulTabbox;
import org.pentaho.ui.xul.containers.XulTabpanels;
import org.pentaho.ui.xul.containers.XulTabs;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;
import org.pentaho.ui.xul.swt.tags.SwtTab;
import org.pentaho.ui.xul.util.XulDialogCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.WeakHashMap;

/**
 * This is basically a copy of the class in use in the Agile BI Project. It handles a tabset for a multi-document
 * perspective.
 * <p/>
 * This class uses the SWT-XUL library to contruct the tabset. This was mainly done for the sake of consistency.
 */
public abstract class AbstractXulPerspective extends AbstractXulEventHandler implements SpoonPerspective, FileListener {

  protected XulDomContainer container;
  protected XulRunner runner;
  protected Document document;
  protected XulTabs tabs;
  protected XulTabpanels panels;
  protected XulTabbox tabbox;
  protected List<SpoonPerspectiveListener> listeners = new ArrayList<SpoonPerspectiveListener>();
  protected EngineMetaInterface selectedMeta;
  protected Map<XulTab, EngineMetaInterface> metas = new WeakHashMap<XulTab, EngineMetaInterface>();
  private Logger logger = LoggerFactory.getLogger( AbstractXulPerspective.class );
  private String defaultExtension = "";

  protected AbstractXulPerspective( String perspectiveSrc ) {
    try {
      SwtXulLoader loader = new SwtXulLoader();
      loader.registerClassLoader( getClass().getClassLoader() );
      container = loader.loadXul( perspectiveSrc, new PDIMessages( this.getClass() ) ); //$NON-NLS-1$

      runner = new SwtXulRunner();
      runner.addContainer( container );
      runner.initialize();

      document = container.getDocumentRoot();
      container.addEventHandler( this );
      tabs = (XulTabs) document.getElementById( "tabs" );
      panels = (XulTabpanels) document.getElementById( "tabpanels" );
      tabbox = (XulTabbox) tabs.getParent();
      BindingFactory bf = new DefaultBindingFactory();
      bf.setDocument( document );
      //      bf.setBindingType(Binding.Type.ONE_WAY);


      bf.createBinding( tabbox, "selectedIndex", this, "selectedMeta",
          new BindingConvertor<Integer, EngineMetaInterface>() {
            public EngineMetaInterface sourceToTarget( Integer value ) {
              return metas.get( tabs.getTabByIndex( value ) );
            }

            public Integer targetToSource( EngineMetaInterface value ) {
              for ( XulTab tab : metas.keySet() ) {
                if ( metas.get( tab ) == value ) {
                  return tab.getParent().getChildNodes().indexOf( tab );
                }
              }
              return -1;
            }
          } );

    } catch ( Exception e ) {
      // TODO: throw exception
      logger.error( "Error initializing perspective", e );
    }
  }

  public static String createShortName( String filename ) {
    if ( filename == null ) {
      return null;
    }
    int extensionPos = filename.lastIndexOf( '.' );
    if ( extensionPos == -1 ) {
      extensionPos = filename.length();
    }
    int sepPos = filename.replace( '\\', '/' ).lastIndexOf( '/' );
    if ( sepPos == -1 ) {
      sepPos = 0;
    } else {
      sepPos++;
    }
    return filename.substring( sepPos, extensionPos );
  }

  public abstract String getDisplayName( Locale l );

  public abstract InputStream getPerspectiveIcon();

  public abstract void createNewTab();

  public void setActive( boolean active ) {
    // With no tabs open there's not much to look at. Lets create one if the tabset is empty.
    if ( tabs.getChildNodes().size() == 0 ) {
      createNewTab();
    }
    for ( SpoonPerspectiveListener l : listeners ) {
      if ( active ) {
        l.onActivation();
      } else {
        l.onDeactication();
      }
    }

  }

  public abstract String getId();

  // ======== File Listener ====== //

  public final Composite getUI() {
    return (Composite) container.getDocumentRoot().getRootElement().getFirstChild().getManagedObject();
  }

  public boolean accepts( String fileName ) {
    if ( fileName == null || fileName.indexOf( '.' ) == -1 ) {
      return false;
    }
    String extension = fileName.substring( fileName.lastIndexOf( '.' ) + 1 );
    return extension.equals( defaultExtension );
  }

  public boolean acceptsXml( String nodeName ) {
    return false;
  }

  public abstract String[] getFileTypeDisplayNames( Locale locale );

  public String getRootNodeName() {
    return null;
  }

  public abstract String[] getSupportedExtensions();

  public abstract boolean save( EngineMetaInterface meta, String fname, boolean isExport );

  public void syncMetaName( EngineMetaInterface meta, String name ) {
  }

  public abstract List<XulEventHandler> getEventHandlers();

  public abstract List<XulOverlay> getOverlays();

  public void addPerspectiveListener( SpoonPerspectiveListener listener ) {
    if ( listeners.contains( listener ) == false ) {
      listeners.add( listener );
    }
  }

  @Override
  public abstract String getName();

  public abstract boolean onTabClose( final int pos ) throws XulException;

  protected String getDefaultExtension() {
    return defaultExtension;
  }

  protected void setDefaultExtension( String defaultExtension ) {
    this.defaultExtension = defaultExtension;
  }

  public XulTabAndPanel createTab( String tabTitle, Object model ) {

    try {
      XulTab tab = (XulTab) document.createElement( "tab" );
      if ( tabTitle != null ) {
        tab.setLabel( tabTitle );
      }
      XulTabpanel panel = (XulTabpanel) document.createElement( "tabpanel" ); //$NON-NLS-1
      panel.setSpacing( 0 );
      panel.setPadding( 0 );

      tabs.addChild( tab );
      panels.addChild( panel );
      //tabbox.setSelectedIndex(panels.getChildNodes().indexOf(panel));

      return new XulTabAndPanel( tab, panel, model );

    } catch ( XulException e ) {
      e.printStackTrace();
    }
    return null;
  }

  public void setNameForTab( XulTab tab, String name ) {
    String tabName = name;
    List<String> usedNames = new ArrayList<String>();
    for ( XulComponent c : tabs.getChildNodes() ) {
      if ( c != tab ) {
        usedNames.add( ( (SwtTab) c ).getLabel() );
      }
    }
    if ( usedNames.contains( name ) ) {
      int num = 2;
      while ( true ) {
        tabName = name + " (" + num + ")";
        if ( usedNames.contains( tabName ) == false ) {
          break;
        }
        num++;
      }
    }

    tab.setLabel( tabName );
  }

  public void setMetaForTab( XulTab tab, EngineMetaInterface meta ) {
    metas.put( tab, meta );
  }

  public EngineMetaInterface getActiveMeta() {
    int idx = tabbox.getSelectedIndex();
    if ( idx == -1 || idx >= tabbox.getTabs().getChildNodes().size() ) {
      return null;
    }
    return metas.get( tabbox.getTabs().getChildNodes().get( idx ) );
  }

  public EngineMetaInterface getSelectedMeta() {
    return selectedMeta;
  }

  public void setSelectedMeta( EngineMetaInterface meta ) {
    EngineMetaInterface prevVal = this.selectedMeta;
    this.selectedMeta = meta;
    Spoon.getInstance().enableMenus();
    firePropertyChange( "selectedMeta", prevVal, meta );
  }

  protected static class CloseConfirmXulDialogCallback implements XulDialogCallback<Object> {
    public boolean closeIt = false;

    public void onClose( XulComponent sender, Status returnCode, Object retVal ) {
      if ( returnCode == Status.ACCEPT ) {
        closeIt = true;
      }
    }

    public void onError( XulComponent sender, Throwable t ) {
    }
  }

  protected static class NameBindingConvertor extends BindingConvertor<String, String> {
    AbstractXulPerspective per;
    XulTab tab;

    public NameBindingConvertor( AbstractXulPerspective per, XulTab tab ) {
      this.per = per;
      this.tab = tab;
    }

    @Override
    public String sourceToTarget( String value ) {
      String tabName = value;
      List<String> usedNames = new ArrayList<String>();
      for ( XulComponent c : per.tabs.getChildNodes() ) {
        if ( c != tab ) {
          usedNames.add( ( (SwtTab) c ).getLabel() );
        }
      }
      if ( usedNames.contains( value ) ) {
        int num = 2;
        while ( true ) {
          tabName = value + " (" + num + ")";
          if ( usedNames.contains( tabName ) == false ) {
            break;
          }
          num++;
        }
      }
      return tabName;
    }

    @Override
    public String targetToSource( String value ) {
      return value;
    }
  }

  public class XulTabAndPanel {
    public XulTab tab;
    public XulTabpanel panel;
    public Object model;

    public XulTabAndPanel( XulTab tab, XulTabpanel panel, Object model ) {
      this.tab = tab;
      this.panel = panel;
      this.model = model;
    }
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
