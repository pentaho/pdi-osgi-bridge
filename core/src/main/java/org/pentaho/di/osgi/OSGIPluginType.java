/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
  public static final String ID = "OSGI_PLUGIN_TYPE";
  public static final String NAME = "Osgi Plugin";

  private static OSGIPluginType pluginType = new OSGIPluginType();

  public static OSGIPluginType getInstance() {
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
    return NAME;
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
