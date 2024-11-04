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
