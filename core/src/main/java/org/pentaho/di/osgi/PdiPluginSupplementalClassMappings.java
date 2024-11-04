/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.osgi;

import java.util.HashMap;
import java.util.Map;

/**
 * Basic Struct class which contains a map of Class to Bean-ID and a reference to the BlueprintContainer.
 *
 * Unforunately due to classloading restrictions we cannot type the container as BlueprintContainer. It is cast
 * later on.
 *
 * Created by nbaker on 3/2/17.
 */
public class PdiPluginSupplementalClassMappings {
  private Map<Class, String> classToBeanNameMap = new HashMap<>(  );
  private Object container;

  public PdiPluginSupplementalClassMappings( Map<Class, String> classToBeanNameMap,
                                             Object container ) {
    this.classToBeanNameMap = classToBeanNameMap;
    this.container = container;
  }

  public Map<Class, String> getClassToBeanNameMap() {
    return classToBeanNameMap;
  }

  public Object getContainer() {
    return container;
  }
}
