/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.osgi;

import org.pentaho.di.osgi.service.lifecycle.LifecycleEvent;

/**
 * User: nbaker Date: 11/17/10
 */
public interface ServiceReferenceListener {
  void serviceEvent( LifecycleEvent eventType, Object serviceObject );
}
