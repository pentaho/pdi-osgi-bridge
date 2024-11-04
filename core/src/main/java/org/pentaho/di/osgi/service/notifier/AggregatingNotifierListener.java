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


package org.pentaho.di.osgi.service.notifier;

import org.pentaho.di.osgi.service.lifecycle.LifecycleEvent;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by bryan on 3/2/16.
 */
public class AggregatingNotifierListener implements DelayedServiceNotifierListener {
  private final AtomicInteger count = new AtomicInteger( 0 );
  private final Set<DelayedServiceNotifierListener>
    listeners = Collections.newSetFromMap( new ConcurrentHashMap<DelayedServiceNotifierListener, Boolean>() );

  public boolean addListener( DelayedServiceNotifierListener notifierListener ) {
    return listeners.add( notifierListener );
  }

  public boolean removeListener( DelayedServiceNotifierListener notifierListener ) {
    return listeners.remove( notifierListener );
  }

  public void incrementCount() {
    count.incrementAndGet();
  }

  public int getCount() {
    return count.get();
  }

  @Override public void onRun( LifecycleEvent event, Object serviceObject ) {
    count.decrementAndGet();
    for ( DelayedServiceNotifierListener listener : listeners ) {
      listener.onRun( event, serviceObject );
    }
  }
}
