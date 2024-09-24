/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *  *******************************************************************************
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 *
 */

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
