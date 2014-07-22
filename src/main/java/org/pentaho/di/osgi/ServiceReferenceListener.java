package org.pentaho.di.osgi;

/**
 * User: nbaker Date: 11/17/10
 */
public interface ServiceReferenceListener {
  void serviceEvent( EVENT_TYPE eventType, Object serviceObject );

  enum EVENT_TYPE { STARTING, STOPPING, MODIFIED}
}
