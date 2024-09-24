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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nbaker on 3/2/17.
 */
public class BlueprintBeanFactory {
  private final String beanId;
  private final Object container;
  private Logger logger = LoggerFactory.getLogger( getClass() );

  public BlueprintBeanFactory( String beanId, Object container) {
    this.beanId = beanId;
    this.container = container;
  }

  public <T> T create( Class<T> pluginClass ) {
    try {
      return OSGIPluginTracker.getInstance().findOrCreateBeanFactoryFor( container ).getInstance( beanId, pluginClass );
    } catch ( OSGIPluginTrackerException e ) {
      logger.error( "Error retriving plugin bean from blueprint container", e );
    }
    return null;
  }
}
