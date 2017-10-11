/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
