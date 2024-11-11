/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.osgi.service.lifecycle;

/**
 * User: nbaker Date: 12/22/10
 */
public interface OSGIServiceLifecycleListener<T> {
  void pluginAdded( T serviceObject );

  void pluginRemoved( T serviceObject );

  void pluginChanged( T serviceObject );
}
