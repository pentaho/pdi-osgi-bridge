package org.pentaho.di.osgi;

/**
 * User: nbaker Date: 12/22/10
 */
public interface OSGIServiceLifecycleListener<T> {
  void pluginAdded( T serviceObject );

  void pluginRemoved( T serviceObject );

  void pluginChanged( T serviceObject );
}
