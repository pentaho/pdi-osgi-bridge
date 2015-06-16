/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
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

    /**
     * Create a ServiceTracker to track the given class in the OSGI Service Registry.
     * @param tracker
     * @param clazzToTrack
     */
    public OSGIServiceTracker(OSGIPluginTracker tracker, Class clazzToTrack) {
        this(tracker, clazzToTrack, false);

    }

    /**
     * Create a ServiceTracker to track the given class in the OSGI Service Registry or a PluginInterface with the
     * PluginType of the given class.
     *
     * @param osgiPluginTracker
     * @param clazzToTrack
     * @param asPluginInterface
     */
    public OSGIServiceTracker(OSGIPluginTracker osgiPluginTracker, Class clazzToTrack, boolean asPluginInterface) {
        super(osgiPluginTracker.getBundleContext(), createFilter( osgiPluginTracker.getBundleContext(), clazzToTrack, asPluginInterface ), null);
        this.tracker = osgiPluginTracker;
        this.clazzToTrack = clazzToTrack;
        this.context = tracker.getBundleContext();
    }

    private static Filter createFilter( BundleContext context, Class<?> clazzToTrack, boolean asPluginInterface){
        try {
            return context.createFilter(
                    asPluginInterface ? "(&(objectClass=" + PluginInterface.class.getName() + ")(PluginType=" + clazzToTrack.getName() + "))" : "(objectClass=" + clazzToTrack.getName() + ")");
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Object addingService(ServiceReference
                                        reference) {
        references.add(reference);
        Object retVal = super.addingService(reference);
        tracker.serviceChanged(clazzToTrack, LifecycleEvent.START, reference);
        return retVal;
    }

    @Override
    public void removedService(ServiceReference
                                       reference, Object service) {
        references.remove(reference);
        super.removedService(reference, service);
        tracker.serviceChanged(clazzToTrack, LifecycleEvent.STOP, reference);
    }

    @Override
    public void modifiedService(ServiceReference
                                        reference, Object service) {
        super.modifiedService(reference, service);
        tracker.serviceChanged(clazzToTrack, LifecycleEvent.MODIFY, reference);
    }
}
