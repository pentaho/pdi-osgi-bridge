package org.pentaho.di.osgi;

import org.pentaho.di.core.plugins.ClassLoadingPluginInterface;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.osgi.BeanFactory;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * User: nbaker
 * Date: 12/9/10
 *
 * This represents a Plugin in the Kettle System that's been registered for a particular PluginTypeInterface.
 *
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

  public OSGIPlugin(){
    
  }
  

  public String getCategory() {
    return category;
  }

  /**
  No meaning in OSGI
   **/
  public Map<Class<?>, String> getClassMap() {
    return Collections.emptyMap();
  }

  public String getDescription() {
    return description;
  }

  public String getErrorHelpFile() {
    return errorHelpFile;
  }

  public String[] getIds() {
    return new String[]{ID};
  }

  public String getImageFile() {
    return imageFile;
  }

  public List<String> getLibraries() {
    return null;
  }

  public Class<?> getMainType() {
    return mainType;
  }

  public String getName() {
    return name;
  }

  public URL getPluginDirectory() {
    return null;
  }

  public Class<? extends PluginTypeInterface> getPluginType() {
    return pluginTypeInterface;
  }

  public boolean isNativePlugin() {
    return false;
  }

  public boolean isSeparateClassLoaderNeeded() {
    return false;
  }

  public boolean matches(String id) {
    return ID.equals(id);
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setErrorHelpFile(String errorHelpFile) {
    this.errorHelpFile = errorHelpFile;
  }

  public void setID(String ID) {
    this.ID = ID;
  }

  public void setImageFile(String imageFile) {
    this.imageFile = imageFile;
  }

  public void setMainType(Class<Object> mainType) {
    this.mainType = mainType;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPluginTypeInterface(Class<PluginTypeInterface> pluginTypeInterface) {
    this.pluginTypeInterface = pluginTypeInterface;
  }

  public <T> T loadClass(Class<T> pluginClass) {
    return OSGIPluginTracker.getInstance().getBean(pluginClass, this, name+"_"+pluginClass.getSimpleName());
  }

  public void setBeanFactory(BeanFactory beanFactory){
    this.beanFactory = beanFactory;
  }

  public BeanFactory getBeanFactory() {
    return beanFactory;
  }
}
