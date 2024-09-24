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

import org.pentaho.platform.servicecoordination.api.IPhasedLifecycleEvent;
import org.pentaho.platform.servicecoordination.api.IPhasedLifecycleListener;
import org.pentaho.platform.servicecoordination.impl.BaseCountdownLatchLifecycleManager;

/**
 * Created by nbaker on 2/18/15.
 */
public class KettlePhaseLifecycleManager extends BaseCountdownLatchLifecycleManager<KettleLifecycleEvent> {


  private static KettlePhaseLifecycleManager INSTANCE = new KettlePhaseLifecycleManager();

  private KettlePhaseLifecycleManager() {
    super( );
  }

  public static KettlePhaseLifecycleManager getInstance(){
    return INSTANCE;
  }

  @Override protected KettleLifecycleEvent getNotificationObject() {
    KettleLifecycleEvent[] values = KettleLifecycleEvent.values();
    int phase = getPhase();
    if ( phase > values.length ) {
      throw new IllegalStateException( "LifecycleManager does not support this phase" );
    }
    return values[ phase ];
  }
}
