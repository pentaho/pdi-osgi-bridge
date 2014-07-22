package org.pentaho.di.osgi;

import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.ClassLoadingPluginInterface;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.osgi.api.BeanFactory;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * User: nbaker Date: 12/9/10
 * <p/>
 * This represents a Plugin in the Kettle System that's been registered for a particular PluginTypeInterface.
 */
public class OSGIPlugin implements PluginInterface, ClassLoadingPluginInterface {

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
  private Map<String, String> classToBeanMap;

  public OSGIPlugin() {

  }

  @Override
  public String getCategory() {
    return category;
  }

  public void setCategory( String category ) {
    this.category = category;
  }

  /**
   * No meaning in OSGI
   */
  @Override
  public Map<Class<?>, String> getClassMap() {
    return Collections.emptyMap();
  }

  @Override
  public String getDescription() {
    return description;
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
    return new String[] { ID };
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
    return null;
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
    return ID.equals( id );
  }

  public void setPluginTypeInterface( Class<PluginTypeInterface> pluginTypeInterface ) {
    this.pluginTypeInterface = pluginTypeInterface;
  }

  @Override
  public <T> T loadClass( Class<T> pluginClass ) throws KettlePluginException {
    String id = classToBeanMap.get( pluginClass.getCanonicalName() );
    if ( id != null ) {
      return OSGIPluginTracker.getInstance().getBean( pluginClass, this, id );
    } else {
      try {
        return pluginClass.newInstance();
      } catch ( Exception e ) {
        throw new KettlePluginException( e );
      }
    }
  }

  public BeanFactory getBeanFactory() {
    return beanFactory;
  }

  public void setBeanFactory( BeanFactory beanFactory ) {
    this.beanFactory = beanFactory;
  }

  @Override
  public ClassLoader getClassLoader() {
    return OSGIPluginTracker.getInstance().getClassLoader( this );
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
    return documentationUrl;
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

  public Map<String, String> getClassToBeanMap() {
    return classToBeanMap;
  }

  public void setClassToBeanMap( Map<String, String> classToBeanMap ) {
    this.classToBeanMap = classToBeanMap;
  }
}
