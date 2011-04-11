package org.pentaho.di.osgi;

/**
 * User: nbaker
 * Date: 11/17/10
 */
public interface ServiceReferenceListener {
  enum EVENT_TYPE{STARTING, STOPPING, MODIFIED}
  void serviceEvent(EVENT_TYPE eventType, Object serviceObject);
}
