/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.osgi.service.tracker;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.osgi.OSGIPluginTracker;
import org.pentaho.di.osgi.service.lifecycle.LifecycleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * User: nbaker Date: 11/11/10
 */
public class OSGIServiceTracker extends ServiceTracker {
  private final Class clazzToTrack;
  private final List<ServiceReference> references = new ArrayList<ServiceReference>();
  private final BundleContext context;
  private final OSGIPluginTracker tracker;
  private static final Logger logger = LoggerFactory.getLogger( OSGIServiceTracker.class );

  /**
   * Create a ServiceTracker to track the given class in the OSGI Service Registry.
   *
   * @param tracker
   * @param clazzToTrack
   */
  public OSGIServiceTracker( OSGIPluginTracker tracker, Class clazzToTrack ) {
    this( tracker, clazzToTrack, false );

  }

  /**
   * Create a ServiceTracker to track the given class in the OSGI Service Registry or a PluginInterface with the
   * PluginType of the given class.
   *
   * @param osgiPluginTracker
   * @param clazzToTrack
   * @param asPluginInterface
   */
  public OSGIServiceTracker( OSGIPluginTracker osgiPluginTracker, Class clazzToTrack, boolean asPluginInterface ) {
    super( osgiPluginTracker.getBundleContext(),
      createFilter( osgiPluginTracker.getBundleContext(), clazzToTrack, asPluginInterface ), null );
    this.tracker = osgiPluginTracker;
    this.clazzToTrack = clazzToTrack;
    this.context = tracker.getBundleContext();
  }

  private static Filter createFilter( BundleContext context, Class<?> clazzToTrack, boolean asPluginInterface ) {
    try {
      return context.createFilter(
        asPluginInterface
          ? "(&(objectClass=" + PluginInterface.class.getName() + ")(PluginType=" + clazzToTrack.getName() + "))"
          : "(objectClass=" + clazzToTrack.getName() + ")" );
    } catch ( InvalidSyntaxException e ) {
      logger.error( "Error creating Service Filter", e );
      return null;
    }
  }

  @Override
  public Object addingService( ServiceReference
                                 reference ) {
    logger.debug( "Called addingService on tracker " + clazzToTrack.getName() );
    logger.debug( "total services tracked " + references.size() );
    references.add( reference );
    tracker.serviceChanged( clazzToTrack, LifecycleEvent.START, reference );
    Object retVal = super.addingService( reference );
    return retVal;
  }

  @Override
  public void removedService( ServiceReference
                                reference, Object service ) {
    logger.debug( "Called removedService on tracker " + clazzToTrack.getName() );
    references.remove( reference );
    // wrapping super call in a method to allow Mockito overriding
    try {
      notifySuperOfRemoval( reference, service );
    } catch ( IllegalStateException ignored ) {
      // This can happen when the service bundle is already stopped. Ignore.
    }
    tracker.serviceChanged( clazzToTrack, LifecycleEvent.STOP, reference );
  }

  void notifySuperOfRemoval( ServiceReference reference, Object service ) {
    super.removedService( reference, service );
  }

  @Override
  public void modifiedService( ServiceReference
                                 reference, Object service ) {
    logger.debug( "Called modifiedService on tracker " + clazzToTrack.getName() );
    tracker.serviceChanged( clazzToTrack, LifecycleEvent.MODIFY, reference );
    super.modifiedService( reference, service );
  }
}
