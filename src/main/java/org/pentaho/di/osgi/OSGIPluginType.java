package org.pentaho.di.osgi;

import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginFolderInterface;
import org.pentaho.di.core.plugins.PluginTypeInterface;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.List;

/**
 * User: nbaker Date: 12/9/10
 */
public class OSGIPluginType implements PluginTypeInterface {

  private static OSGIPluginType pluginType;
  private String ID;
  private String name;

  public OSGIPluginType() {
    ID = "OSGI_PLUGIN_TYPE";
    name = "Osgi Plugin";
  }

  public static OSGIPluginType getInstance() {
    if ( pluginType == null ) {
      pluginType = new OSGIPluginType();
    }
    return pluginType;
  }

  /**
   * No meaning in OSGI
   */
  @Override
  public void addObjectType( Class<?> clz, String xmlNodeName ) {
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getName() {
    return name;
  }

  /**
   * No meaning in OSGI
   */
  @Override
  public List<PluginFolderInterface> getPluginFolders() {
    return null;
  }

  /**
   * No meaning in OSGI
   */
  @Override
  public void handlePluginAnnotation( Class<?> clazz, Annotation annotation, List<String> libraries,
                                      boolean nativePluginType, URL pluginFolder ) throws KettlePluginException {

  }

  @Override
  public void searchPlugins() throws KettlePluginException {

  }
}
