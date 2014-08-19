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

package org.pentaho.di.osgi;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.osgi.service.lifecycle.LifecycleEvent;
import org.pentaho.di.osgi.service.lifecycle.OSGIServiceLifecycleListener;
import org.pentaho.di.osgi.service.listener.BundleContextServiceListener;
import org.pentaho.di.osgi.service.notifier.DelayedInstanceNotifierFactory;
import org.pentaho.di.osgi.service.notifier.DelayedServiceNotifier;
import org.pentaho.di.osgi.service.tracker.OSGIServiceTracker;
import org.pentaho.osgi.api.BeanFactory;
import org.pentaho.osgi.api.BeanFactoryLocator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * User: nbaker Date: 11/9/10
 */
public class OSGIPluginTracker {
  private static OSGIPluginTracker INSTANCE = new OSGIPluginTracker();
  private BundleContext context;
  private BeanFactoryLocator lookup;
  private Map<Class, OSGIServiceTracker> trackers = new WeakHashMap<Class, OSGIServiceTracker>();
  private Map<Class, List<OSGIServiceLifecycleListener>> listeners =
    new WeakHashMap<Class, List<OSGIServiceLifecycleListener>>();
  private Map<Object, ServiceReference> instanceToReferenceMap = new WeakHashMap<Object, ServiceReference>();
  private Map<ServiceReference, Object> referenceToInstanceMap = new WeakHashMap<ServiceReference, Object>();
  private Map<BeanFactory, Bundle> beanFactoryToBundleMap = new WeakHashMap<BeanFactory, Bundle>();
  private Map<Object, BeanFactory> beanToFactoryMap = new WeakHashMap<Object, BeanFactory>();
  private Map<Object, List<ServiceReferenceListener>> instanceListeners =
    new WeakHashMap<Object, List<ServiceReferenceListener>>();
  private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private Log logger = LogFactory.getLog( getClass().getName() );
  private List<Class<? extends PluginTypeInterface>> queuedClasses =
    new ArrayList<Class<? extends PluginTypeInterface>>();

  // ONLY CALL EXTERNALLY FOR UNIT TESTS
  protected OSGIPluginTracker() {

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
      ServiceReference[] refs =
        cxt.getServiceReferences( PluginInterface.class.getName(), "(PluginType=" + pluginType.getName() + ")" );
      if ( refs != null && refs.length > 0 ) {
        return BeanUtils.getProperty( cxt.getService( refs[ 0 ] ), prop );
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

  public BeanFactory findOrCreateBeanFactoryFor( Object serviceObject ) {
    ServiceReference reference = instanceToReferenceMap.get( serviceObject );
    if ( reference == null || lookup == null ) {
      return null;
    }
    Bundle objectBundle = reference.getBundle();
    BeanFactory factory = lookup.getBeanFactory( objectBundle );
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
    context.addServiceListener( new BundleContextServiceListener( referenceToInstanceMap,
      new DelayedInstanceNotifierFactory( instanceListeners, scheduler, this ) ) );

    for ( Class<? extends PluginTypeInterface> type : queuedClasses ) {
      registerPluginClass( type );
    }

    OSGIKettleLifecycleListener.setDoneInitializing();
  }

  public boolean registerPluginClass( Class clazz ) {
    if ( listeners.get( clazz ) != null ) {
      // Already tracking
      return true;
    }
    if ( this.getBundleContext() == null ) {
      queuedClasses.add( clazz );
      return false;
    }
    listeners.put( clazz, new ArrayList<OSGIServiceLifecycleListener>() );

    OSGIServiceTracker tracker = new OSGIServiceTracker( this, clazz );
    tracker.open();
    trackers.put( clazz, tracker );
    for ( PluginInterface plugin : getServiceObjects( PluginInterface.class,
      Collections.singletonMap( "PluginType",
        clazz.getName() ) ) ) {
      try {
        PluginRegistry.getInstance().registerPlugin( clazz, plugin );
      } catch ( KettlePluginException e ) {
        e.printStackTrace();
      }
    }
    return true;
  }

  public void serviceChanged( Class<?> cls, LifecycleEvent evt, ServiceReference serviceObject ) {
    Object instance = context.getService( serviceObject );
    instanceToReferenceMap.put( instance, serviceObject );
    new DelayedServiceNotifier( this, cls, evt, instance, listeners, scheduler ).run();
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
