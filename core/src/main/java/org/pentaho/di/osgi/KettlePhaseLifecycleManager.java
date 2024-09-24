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
