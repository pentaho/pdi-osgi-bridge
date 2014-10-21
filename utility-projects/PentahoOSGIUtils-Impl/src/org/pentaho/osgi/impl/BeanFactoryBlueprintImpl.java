package org.pentaho.osgi.impl;

import org.osgi.service.blueprint.container.BlueprintContainer;
import org.pentaho.osgi.api.BeanFactory;

/**
 * User: nbaker
 * Date: 11/30/10
 */
public class BeanFactoryBlueprintImpl implements BeanFactory {
  private BlueprintContainer blueprintContainer;

  public BeanFactoryBlueprintImpl(BlueprintContainer blueprintContainer){
    this.blueprintContainer = blueprintContainer;
  }
  @Override
  public Object getInstance(String id) {
    return blueprintContainer.getComponentInstance(id);
  }

  @Override
  public <T> T getInstance(String id, Class<T> classType) {
    Object beanFromContainer = blueprintContainer.getComponentInstance(id);
//    if(beanFromContainer.getClass().isAssignableFrom(classType.getClass()) == false){
//      throw new IllegalStateException(MessageFormat.format("Component in the bean container with id: {0} doesn't match required type: {1}", id, classType.getClass().getName()));
//    }
    return (T) beanFromContainer;
  }
}
