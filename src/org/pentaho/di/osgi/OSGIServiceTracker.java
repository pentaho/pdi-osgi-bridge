package org.pentaho.di.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import java.util.ArrayList;
import java.util.List;

/**
 * User: nbaker
 * Date: 11/11/10
 */
public class OSGIServiceTracker extends ServiceTracker{

  private Class clazzToTrack;

  private List<ServiceReference> references = new ArrayList<ServiceReference>();
  private BundleContext context;
  private OSGIPluginTracker tracker;

  public OSGIServiceTracker(OSGIPluginTracker tracker, Class clazzToTrack){
    super(tracker.getBundleContext(), clazzToTrack.getName(), null);
    this.tracker = tracker;
    this.clazzToTrack = clazzToTrack;
    this.context = tracker.getBundleContext();
  }

  @Override
  public Object addingService(ServiceReference
    reference) {
    references.add(reference);

    Object retVal =  super.addingService(reference);
    tracker.serviceChanged(clazzToTrack, OSGIPluginTracker.Event.START, reference);
    return retVal;
  }

  @Override
  public void removedService(ServiceReference
    reference, Object service) {
    references.remove(reference);
    super.removedService(reference, service);
    tracker.serviceChanged(clazzToTrack, OSGIPluginTracker.Event.STOP, reference);
  }

  @Override
  public void modifiedService(ServiceReference
    reference, Object service) {
    super.modifiedService(reference, service);
    tracker.serviceChanged(clazzToTrack, OSGIPluginTracker.Event.MODIFY, reference);
  }

}