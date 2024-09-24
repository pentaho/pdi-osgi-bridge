/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
