package org.pentaho.osgi.impl;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.pentaho.osgi.api.BeanFactory;
import org.pentaho.osgi.api.BeanFactoryLocator;

/**
 * User: nbaker
 * Date: 12/17/10
 */
public class BeanFactoryLocatorImpl implements BeanFactoryLocator {
  private static final String CONTAINER_KEY = "org.osgi.service.blueprint.container.BlueprintContainer";
  @Override
  public BeanFactory getBeanFactory(Bundle bundle) {
    for(ServiceReference ref : bundle.getRegisteredServices()){
      for(String clazz : ((String[])ref.getProperty("objectClass"))){
        if(clazz.equals(CONTAINER_KEY)){
          return new BeanFactoryBlueprintImpl((BlueprintContainer) bundle.getBundleContext().getService(ref));
        }
      }
    }

    return null;
  }
}
