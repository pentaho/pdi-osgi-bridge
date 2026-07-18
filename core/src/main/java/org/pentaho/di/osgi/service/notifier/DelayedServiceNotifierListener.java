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



package org.pentaho.di.osgi.service.notifier;

import org.pentaho.di.osgi.service.lifecycle.LifecycleEvent;

/**
 * Created by bryan on 3/2/16.
 */
public interface DelayedServiceNotifierListener {
  void onRun( LifecycleEvent event, Object serviceObject );
}
