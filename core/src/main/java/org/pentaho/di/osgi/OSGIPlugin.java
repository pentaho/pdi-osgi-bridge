/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.di.core.Const;
import org.pentaho.di.core.plugins.ClassLoadingPluginInterface;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.osgi.api.BeanFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * User: nbaker Date: 12/9/10
 * <p/>
 * This represents a Plugin in the Kettle System that's been registered for a particular PluginTypeInterface.
 */
public class OSGIPlugin implements PluginInterface, ClassLoadingPluginInterface {

  private OSGIPluginTracker osgiPluginTracker;
  private String category;
  private String description;
  private String errorHelpFile;
  private String ID;
  private String name;
  private String imageFile;
  private Class<Object> mainType;
  private Class<PluginTypeInterface> pluginTypeInterface;
  private BeanFactory beanFactory;
  private String casesUrl;
  private String documentationUrl;
  private String forumUrl;
  private Map<Class<?>, BlueprintBeanFactory> classFactoryMap = new HashMap<>();

  private Logger logger = LoggerFactory.getLogger( getClass() );

  public OSGIPlugin() {
    osgiPluginTracker = OSGIPluginTracker.getInstance();
  }

  protected void setOsgiPluginTracker( OSGIPluginTracker osgiPluginTracker ) {
    this.osgiPluginTracker = osgiPluginTracker;
  }

  @Override
  public String getCategory() {
    return translateString( category );
  }

  private static String translateString( String str ) {

    if ( str == null ) {
      return null;
    }

    if ( str.startsWith( "i18n:" ) ) {
      String[] parts = str.split( ":" );
      if ( parts.length != 3 ) {
        return str;
      } else {
        return BaseMessages.getString( parts[ 1 ], parts[ 2 ] );
      }
    }

    return str;
  }

  public void setCategory( String category ) {
    this.category = category;
  }

  /**
   * Not sure the purpose of this.
   */
  @Override
  public Map<Class<?>, String> getClassMap() {

    return classFactoryMap.keySet().stream()
      .collect( Collectors.toMap( Function.identity(), aClass -> loadClass( aClass ).getClass().getName() ) );

  }

  @Override
  public String getDescription() {
    return translateString( description );
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  @Override
  public String getErrorHelpFile() {
    return errorHelpFile;
  }

  public void setErrorHelpFile( String errorHelpFile ) {
    this.errorHelpFile = errorHelpFile;
  }

  @Override
  public String[] getIds() {
    return new String[] { getID() };
  }

  public String getID() {
    return ID;
  }

  public void setID( String ID ) {
    this.ID = ID;
  }

  @Override
  public String getImageFile() {
    return imageFile;
  }

  public void setImageFile( String imageFile ) {
    this.imageFile = imageFile;
  }

  @Override
  public List<String> getLibraries() {
    return Collections.emptyList();
  }

  @Override
  public Class<?> getMainType() {
    return mainType;
  }

  public void setMainType( Class<Object> mainType ) {
    this.mainType = mainType;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  @Override
  public URL getPluginDirectory() {
    return null;
  }

  @Override
  public Class<? extends PluginTypeInterface> getPluginType() {
    return pluginTypeInterface;
  }

  @Override
  public boolean isNativePlugin() {
    return false;
  }

  @Override
  public boolean isSeparateClassLoaderNeeded() {
    return false;
  }

  @Override
  public boolean matches( String id ) {
    return getID().equals( id );
  }

  public void setPluginTypeInterface( Class<PluginTypeInterface> pluginTypeInterface ) {
    this.pluginTypeInterface = pluginTypeInterface;
  }

  @Override
  public <T> T loadClass( Class<T> pluginClass ) {
    if ( classFactoryMap.containsKey( pluginClass ) ) {
      return classFactoryMap.get( pluginClass ).create( pluginClass );
    }

    return null;
  }

  public BeanFactory getBeanFactory() {
    return beanFactory;
  }

  public void setBeanFactory( BeanFactory beanFactory ) {
    this.beanFactory = beanFactory;
  }

  @Override
  public ClassLoader getClassLoader() {
    return osgiPluginTracker.getClassLoader( this );
  }

  @Override
  public String getCasesUrl() {
    return casesUrl;
  }

  @Override
  public void setCasesUrl( String casesUrl ) {
    this.casesUrl = casesUrl;
  }

  @Override
  public String getDocumentationUrl() {
    return Const.getDocUrl( documentationUrl );
  }

  @Override
  public void setDocumentationUrl( String documentationUrl ) {
    this.documentationUrl = documentationUrl;
  }

  @Override
  public String getForumUrl() {
    return forumUrl;
  }

  @Override
  public void setForumUrl( String forumUrl ) {
    this.forumUrl = forumUrl;
  }

  @Override
  public String getClassLoaderGroup() {
    return null;
  }

  @Override
  public void setClassLoaderGroup( String arg0 ) {
    // noop
  }

  @Override
  public void setSuggestion( String suggestion ) {
  }

  @Override
  public String getSuggestion() {
    return null;
  }

  public void setClassToBeanMap( Map<Class, String> classToBeanMap ) {
    classToBeanMap.forEach( ( aClass, s ) -> addClassFactory( aClass, new BlueprintBeanFactory( s, this ) ) );
  }

  public void addClassFactory( Class<?> aClass, BlueprintBeanFactory blueprintBeanFactory ) {
    classFactoryMap.put( aClass, blueprintBeanFactory );
  }
}
