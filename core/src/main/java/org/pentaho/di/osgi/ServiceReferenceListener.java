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

import org.pentaho.di.osgi.service.lifecycle.LifecycleEvent;

/**
 * User: nbaker Date: 11/17/10
 */
public interface ServiceReferenceListener {
  void serviceEvent( LifecycleEvent eventType, Object serviceObject );
}
