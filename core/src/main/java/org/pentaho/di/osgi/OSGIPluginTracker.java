/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.osgi;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.MapMaker;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.osgi.service.lifecycle.LifecycleEvent;
import org.pentaho.di.osgi.service.lifecycle.OSGIServiceLifecycleListener;
import org.pentaho.di.osgi.service.listener.BundleContextServiceListener;
import org.pentaho.di.osgi.service.notifier.AggregatingNotifierListener;
import org.pentaho.di.osgi.service.notifier.DelayedInstanceNotifierFactory;
import org.pentaho.di.osgi.service.notifier.DelayedServiceNotifier;
import org.pentaho.di.osgi.service.notifier.DelayedServiceNotifierListener;
import org.pentaho.di.osgi.service.tracker.OSGIServiceTracker;
import org.pentaho.osgi.api.BeanFactory;
import org.pentaho.osgi.api.BeanFactoryLocator;
import org.pentaho.osgi.api.ProxyUnwrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * User: nbaker Date: 11/9/10
 */
public class OSGIPluginTracker {
  private static OSGIPluginTracker INSTANCE = new OSGIPluginTracker();
  private static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor( new ThreadFactory() {
    @Override
    public Thread newThread( Runnable r ) {

      Thread thread = Executors.defaultThreadFactory().newThread( r );
      thread.setDaemon( true );
      thread.setName( "OSGIPluginTracker pool" );
      return thread;
    }
  } );
  private final AggregatingNotifierListener aggregatingNotifierListener;
  private BundleContext context;
  private BeanFactoryLocator lookup;
  private ProxyUnwrapper proxyUnwrapper;
  private Map<Class, OSGIServiceTracker> trackers = new WeakHashMap<Class, OSGIServiceTracker>();
  private Map<Class, List<OSGIServiceLifecycleListener>> listeners =
    new WeakHashMap<Class, List<OSGIServiceLifecycleListener>>();
  private Map<Object, List<ServiceReferenceListener>> instanceListeners =
    new WeakHashMap<Object, List<ServiceReferenceListener>>();
  // MapMaker provides a ConcurrentMap with weak keys and weak values
  private Map<Object, ServiceReference> instanceToReferenceMap =
    new MapMaker().weakKeys().weakValues().makeMap();
  private Map<ServiceReference, Object> referenceToInstanceMap =
    new MapMaker().weakKeys().weakValues().makeMap();
  private Map<BeanFactory, Bundle> beanFactoryToBundleMap =
    new MapMaker().weakKeys().weakValues().makeMap();
  private Map<Object, BeanFactory> beanToFactoryMap =
    new MapMaker().weakKeys().weakValues().makeMap();
  private Log logger = LogFactory.getLog( getClass().getName() );
  private List<Class<? extends PluginTypeInterface>> queuedClasses =
    new ArrayList<Class<? extends PluginTypeInterface>>();

  // ONLY CALL EXTERNALLY FOR UNIT TESTS
  @VisibleForTesting
  protected OSGIPluginTracker() {
    this( new AggregatingNotifierListener() );
  }

  @VisibleForTesting
  protected OSGIPluginTracker( AggregatingNotifierListener aggregatingNotifierListener ) {
    this.aggregatingNotifierListener = aggregatingNotifierListener;
  }

  public static OSGIPluginTracker getInstance() {
    return INSTANCE;
  }

  public <T> List<T> getServiceObjects( Class<T> clazz, Map<String, String> props ) {
    List<T> services = new ArrayList<T>();

    String propsString = createFilterString( props );
    try {
      ServiceReference[] refs = context.getServiceReferences( clazz.getName(), propsString );
      if ( refs == null ) {
        return Collections.emptyList();
      }
      for ( ServiceReference ref : refs ) {
        T serv = (T) context.getService( ref );

        instanceToReferenceMap.put( serv, ref );
        referenceToInstanceMap.put( ref, serv );

        services.add( serv );
      }
    } catch ( InvalidSyntaxException e ) {
      logger.error( e.getMessage(), e );
    }
    return services;
  }

  private String createFilterString( Map<String, String> props ) {
    StringBuffer sb = new StringBuffer();
    boolean firstProp = true;
    for ( Map.Entry<String, String> prop : props.entrySet() ) {
      if ( firstProp ) {
        sb.append( "(" );
      }
      // if(!firstProp){
      sb.append( "&" );
      // }
      sb.append( "(" + prop.getKey() + "=" + prop.getValue() + ")" );
      if ( firstProp ) {
        sb.append( ")" );
      }
      firstProp = false;
    }
    return sb.toString();
  }

  public <T> T getBean( Class<T> clazz, Object serviceClass, String id ) {

    BeanFactory factory = findOrCreateBeanFactoryFor( serviceClass );
    if ( factory == null ) {
      return null;
    }
    T instance = factory.getInstance( id, clazz );
    beanToFactoryMap.put( instance, factory );
    return instance;
  }

  /**
   * Very limited. currently used to get the name of the plugin.
   *
   * @param pluginType
   * @param instance   a bean instance from the bundle blueprint
   * @param prop       name of the property to extract
   * @return bean property
   */
  public <T extends PluginTypeInterface> Object getBeanPluginProperty( Class<? extends PluginTypeInterface> pluginType,
                                                                       Object instance, String prop ) {
    try {
      BeanFactory beanFactory = beanToFactoryMap.get( instance );
      if ( beanFactory == null ) {
        return null;
      }

      Bundle bundle = beanFactoryToBundleMap.get( beanFactory );
      BundleContext cxt = bundle.getBundleContext();

      ServiceReference[] registeredServices = bundle.getRegisteredServices();
      if ( registeredServices == null ) {
        return null;
      }
      for ( ServiceReference registeredService : registeredServices ) {
        Object registeredServiceProperty = registeredService.getProperty( "objectClass" );
        String proVal = ( registeredServiceProperty instanceof String ) ? (String) registeredServiceProperty
          : ( (String[]) registeredServiceProperty )[ 0 ];
        if ( proVal.equals( PluginInterface.class.getName() )
          && registeredService.getProperty( "PluginType" ).equals( pluginType.getName() ) ) {
          Object service = cxt.getService( registeredService );
          if ( service instanceof OSGIPlugin ) {
            if ( "ID".equalsIgnoreCase( prop ) ) {
              return ( (OSGIPlugin) service ).getID();
            }
          }
          return BeanUtils.getProperty( service, prop );
        }
      }
    } catch ( Exception e ) {
      logger.error( e.getMessage(), e );
    }
    return null;
  }

  public ClassLoader getClassLoader( Object instance ) {
    try {
      ServiceReference ref = instanceToReferenceMap.get( instance );
      if ( ref == null ) {
        return null;
      }
      Bundle bundle = ref.getBundle();
      return new BundleClassloaderWrapper( bundle );

    } catch ( Exception e ) {
      logger.error( e.getMessage(), e );
    }
    return null;
  }

  public void setBeanFactoryLookup( BeanFactoryLocator lookup ) {
    this.lookup = lookup;
  }

  public ProxyUnwrapper getProxyUnwrapper() {
    if ( proxyUnwrapper == null ) {
      ServiceTracker<ProxyUnwrapper, ProxyUnwrapper> tracker =
        new ServiceTracker( context, ProxyUnwrapper.class, null );
      tracker.open();
      try {
        tracker.waitForService( 30000 );
        proxyUnwrapper = tracker.getService();
      } catch ( InterruptedException e ) {
        throw new NullPointerException( "No ProxyUnwrapper found" );
      } finally {
        tracker.close();
      }
    }
    return proxyUnwrapper;
  }

  public void setProxyUnwrapper( ProxyUnwrapper proxyUnwrapper ) {
    this.proxyUnwrapper = proxyUnwrapper;
  }

  public BeanFactory findOrCreateBeanFactoryFor( Object serviceObject ) {
    ServiceReference reference = instanceToReferenceMap.get( serviceObject );
    if ( reference == null || lookup == null ) {
      return null;
    }
    Bundle objectBundle = reference.getBundle();
    BeanFactory factory = lookup.getBeanFactory( objectBundle );
    if ( factory == null ) {
      return null;
    }
    beanFactoryToBundleMap.put( factory, objectBundle );
    return factory;
  }

  public void shutdown() {
    for ( Map.Entry<Class, OSGIServiceTracker> entry : trackers.entrySet() ) {
      entry.getValue().close();
    }
  }

  public void addPluginLifecycleListener( Class clazzToTrack, OSGIServiceLifecycleListener listener ) {
    List<OSGIServiceLifecycleListener> list = listeners.get( clazzToTrack );
    if ( list == null ) {
      list = new ArrayList<OSGIServiceLifecycleListener>();
      listeners.put( clazzToTrack, list );
    }
    list.add( listener );
  }

  public BundleContext getBundleContext() {
    return context;
  }

  public void setBundleContext( BundleContext context ) {
    this.context = context;
    for ( OSGIServiceTracker tracker : trackers.values() ) {
      tracker.close();
    }
    trackers.clear();

    // Not sure who is watching instances, instancesListeners never seems to be modified.
    // TODO: Verify not needed then remove
    context.addServiceListener( new BundleContextServiceListener( referenceToInstanceMap,
      new DelayedInstanceNotifierFactory( instanceListeners, scheduler, this ) ) );

    for ( Class<? extends PluginTypeInterface> type : queuedClasses ) {
      registerPluginClass( type );
    }

    OSGIKettleLifecycleListener.setDoneInitializing();
  }

  public boolean registerPluginClass( Class clazz ) {
    if ( trackers.get( clazz ) != null ) {
      // Already tracking
      return true;
    }
    if ( this.getBundleContext() == null ) {
      queuedClasses.add( clazz );
      return false;
    }
    if ( listeners.get( clazz ) == null ) {
      listeners.put( clazz, new ArrayList<OSGIServiceLifecycleListener>() );
    }

    // Track the OsgiPlugin (PluginInterface) directly. This triggers PluginRegistry.registerPlugin()
    OSGIServiceTracker tracker = new OSGIServiceTracker( this, clazz );
    tracker.open();

    // Track it as a PluginInterface with a PluginType of the given class. This one trigger type trackers in pdi.
    // We're obscuring the other tracker, but the 'trackers' collection is just a marker
    tracker = new OSGIServiceTracker( this, clazz, true );
    tracker.open();

    trackers.put( clazz, tracker );

    return true;
  }

  public void serviceChanged( Class<?> cls, LifecycleEvent evt, ServiceReference serviceObject ) {
    Object instance = context.getService( serviceObject );
    instance = getProxyUnwrapper().unwrap( instance );
    instanceToReferenceMap.put( instance, serviceObject );
    aggregatingNotifierListener.incrementCount();
    new DelayedServiceNotifier( this, cls, evt, instance, listeners, scheduler, aggregatingNotifierListener ).run();
  }

  public boolean addDelayedServiceNotifierListener( DelayedServiceNotifierListener delayedServiceNotifierListener ) {
    return aggregatingNotifierListener.addListener( delayedServiceNotifierListener );
  }

  public boolean removeDelayedServiceNotifierListener( DelayedServiceNotifierListener delayedServiceNotifierListener ) {
    return aggregatingNotifierListener.removeListener( delayedServiceNotifierListener );
  }

  public int getOutstandingServiceNotifierListeners() {
    return aggregatingNotifierListener.getCount();
  }

  // FOR UNIT TEST ONLY
  protected void setLogger( Log logger ) {
    this.logger = logger;
  }

  // FOR UNIT TEST ONLY
  protected Map<Class, OSGIServiceTracker> getTrackers() {
    return trackers;
  }
}
