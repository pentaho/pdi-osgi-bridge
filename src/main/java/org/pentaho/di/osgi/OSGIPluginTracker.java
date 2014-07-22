package org.pentaho.di.osgi;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginRegistryExtension;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.core.plugins.RegistryPlugin;
import org.pentaho.di.karaf.KarafHost;
import org.pentaho.osgi.api.BeanFactory;
import org.pentaho.osgi.api.BeanFactoryLocator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * User: nbaker Date: 11/9/10
 */
@RegistryPlugin( id = "OSGIRegistryPlugin", name = "OSGI" )
public class OSGIPluginTracker implements PluginRegistryExtension {

  private static OSGIPluginTracker INSTANCE;
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
  private List<PluginTypeInterface> queuedTypes = new ArrayList<PluginTypeInterface>();
  private Map<Class, ServiceRegistration> registeredServices = new HashMap<Class, ServiceRegistration>();

  // As this class is constructed by the kettle plugin system it's constructor must be available. We cannot have Kettle
  // use a factory method unfortunately.
  public OSGIPluginTracker() {
    INSTANCE = this;
  }

  public static OSGIPluginTracker getInstance() {
    if ( INSTANCE == null ) {
      INSTANCE = new OSGIPluginTracker();
    }
    return INSTANCE;
  }

  @Override
  public void init( final PluginRegistry registry ) {
    KarafHost.getInstance();
    if ( KettleClientEnvironment.isInitialized() ) {
      PluginRegistry.addPluginType( OSGIPluginType.getInstance() );
      registerPluginClass( PluginInterface.class );
      addPluginLifecycleListener( PluginInterface.class, new OSGIServiceLifecycleListener<PluginInterface>() {
        @Override
        public void pluginAdded( PluginInterface serviceObject ) {
          try {
            OSGIPlugin osgiPlugin = (OSGIPlugin) serviceObject;
            Class<? extends PluginTypeInterface> pluginTypeFromPlugin = osgiPlugin.getPluginType();
            try {
              registry.registerPlugin( pluginTypeFromPlugin, serviceObject );
            } catch ( KettlePluginException e ) {
              e.printStackTrace();
            }
          } catch ( Exception e ) {
            logger.error( "Error notifying listener of plugin addition", e );
          }
        }

        @Override
        public void pluginRemoved( PluginInterface serviceObject ) {
          try {
            OSGIPlugin osgiPlugin = (OSGIPlugin) serviceObject;
            Class<? extends PluginTypeInterface> pluginTypeFromPlugin = osgiPlugin.getPluginType();
            registry.removePlugin( pluginTypeFromPlugin, serviceObject );
          } catch ( Exception e ) {
            logger.error( "Error notifying listener of plugin removal", e );
          }
        }

        @Override
        public void pluginChanged( PluginInterface serviceObject ) {
          // No nothing
        }
      } );

      for ( PluginTypeInterface type : queuedTypes ) {
        searchForType( type );
      }
    }
  }

  @Override
  public void searchForType( PluginTypeInterface pluginType ) {
    if ( this.getBundleContext() == null ) {
      queuedTypes.add( pluginType );
      return;
    }
    registerPluginClass( pluginType.getClass() );
    for ( PluginInterface plugin : getServiceObjects( PluginInterface.class, Collections.singletonMap( "PluginType",
      pluginType.getClass().getName() ) ) ) {
      try {
        PluginRegistry.getInstance().registerPlugin( pluginType.getClass(), plugin );
      } catch ( KettlePluginException e ) {
        e.printStackTrace();
      }
    }

  }

  @Override
  public String getPluginId( Class<? extends PluginTypeInterface> pluginType, Object pluginClass ) {
    try {
      return (String) OSGIPluginTracker.getInstance().getBeanPluginProperty( pluginType, pluginClass, "ID" );
    } catch ( Exception e ) {
      e.printStackTrace();
    }
    return null;

  }

  public <T> List<T> getServiceObjects( Class<T> clazz ) {
    List<T> serviceObjects = new ArrayList<T>();
    ServiceReference[] refs = trackers.get( clazz ).getServiceReferences();
    if ( refs == null ) {
      return Collections.emptyList();
    }
    for ( ServiceReference r : refs ) {
      T serv = (T) context.getService( r );

      instanceToReferenceMap.put( serv, r );
      referenceToInstanceMap.put( r, serv );

      serviceObjects.add( serv );
    }
    return serviceObjects;
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
      e.printStackTrace();
    }
    return services;
  }

  public <T> T getServiceObject( Class<T> clazz, Map<String, String> props ) {
    return getServiceObject( clazz, props, true );
  }

  public <T> T getServiceObject( Class<T> clazz, Map<String, String> props, boolean createProxy ) {
    try {
      Hashtable<String, Object> env = new Hashtable<String, Object>();
      env.put( "osgi.service.jndi.bundleContext", context );
      Context ctx = new InitialContext( env );

      String propsString = createFilterString( props );
      T retVal = null;
      if ( createProxy ) {
        retVal = (T) ctx.lookup( "osgi:service/" + clazz.getName() + "/" + propsString );
      } else {
        retVal = (T) ctx.lookup( "aries:services/" + clazz.getName() + "/" + propsString );
      }

      ServiceReference[] refs = context.getServiceReferences( clazz.getName(), propsString );

      if ( retVal != null && refs != null && refs.length > 0 ) {
        instanceToReferenceMap.put( retVal, refs[ refs.length - 1 ] );
        referenceToInstanceMap.put( refs[ refs.length - 1 ], retVal );
      }

      return retVal;

    } catch ( NamingException e ) {
      e.printStackTrace();
    } catch ( InvalidSyntaxException e ) {
      e.printStackTrace();
    }
    return null;
  }

  public Object getServiceProperty( Object service, String prop ) {
    return instanceToReferenceMap.get( service ).getProperty( prop );
  }

  // TODO: lots of checking to add here
  // public <T> T getServiceObject(Class<T> clazz, String name){
  // try {
  // Hashtable<String, Object> env = new Hashtable<String, Object>();
  // env.put("osgi.service.jndi.bundleContext", context );
  // Context ctx = new InitialContext(env);
  //
  // return (T)ctx.lookup("osgi:service/"+clazz.getName()+"/(stepName=" + name + ")");
  //
  // } catch (NamingException e) {
  // e.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
  // }
  // return null;
  // }

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

  public void addServiceReferenceListener( Object obj, ServiceReferenceListener listener ) {
    List<ServiceReferenceListener> listeners;
    if ( instanceListeners.containsKey( obj ) == false ) {
      listeners = new ArrayList<ServiceReferenceListener>();
      instanceListeners.put( obj, listeners );
    } else {
      listeners = instanceListeners.get( obj );
    }
    listeners.add( listener );
  }

  public InputStream getResourceAsStream( Object serviceObject, String resource ) {
    ServiceReference reference = instanceToReferenceMap.get( serviceObject );
    if ( reference == null ) {
      return null;
    }
    Bundle objectBundle = reference.getBundle();
    URL url = objectBundle.getResource( resource );

    try {
      return url.openStream();
    } catch ( IOException e ) {
      e.printStackTrace();
    }
    return null;
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

  public BeanFactory getBeanFactory( Object bean ) {
    return beanToFactoryMap.get( bean );
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
      e.printStackTrace();
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
      e.printStackTrace();
    }
    return null;
  }

  public void setBeanFactoryLookup( BeanFactoryLocator lookup ) {
    this.lookup = lookup;
  }

  private BeanFactory findOrCreateBeanFactoryFor( Object serviceObject ) {
    ServiceReference reference = instanceToReferenceMap.get( serviceObject );
    if ( reference == null || lookup == null ) {
      return null;
    }
    Bundle objectBundle = reference.getBundle();
    BeanFactory factory = lookup.getBeanFactory( objectBundle );
    beanFactoryToBundleMap.put( factory, objectBundle );

    return factory;
  }

  public <T> T getBean( Class<T> clazz, Map<String, String> beanFactoryProps, String id ) {
    BeanFactory factory = getServiceObject( BeanFactory.class, beanFactoryProps, false );

    T instance = factory.getInstance( id, clazz );
    beanToFactoryMap.put( instance, factory );
    return instance;
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
    context.addServiceListener( new ServiceListener() {

      @Override
      public void serviceChanged( ServiceEvent serviceEvent ) {
        if ( referenceToInstanceMap.containsKey( serviceEvent.getServiceReference() ) ) {
          Object instance = referenceToInstanceMap.get( serviceEvent.getServiceReference() );
          ServiceReferenceListener.EVENT_TYPE type = ServiceReferenceListener.EVENT_TYPE.MODIFIED;

          switch( serviceEvent.getType() ) {
            case ServiceEvent.MODIFIED:
              type = ServiceReferenceListener.EVENT_TYPE.MODIFIED;
              break;
            case ServiceEvent.UNREGISTERING:
              type = ServiceReferenceListener.EVENT_TYPE.STOPPING;
              break;
          }
          List<ServiceReferenceListener> listeners = instanceListeners.get( instance );
          if ( listeners == null || listeners.size() == 0 ) {
            return;
          }

          // The beanfactory may not be registered yet. If not schedule a check every second until it is.
          BeanFactory factory = findOrCreateBeanFactoryFor( instance );
          if ( factory == null ) {
            ScheduledFuture<?> timeHandle =
              scheduler.schedule( new DelayedInstanceNotifier( type, instance ), 2, TimeUnit.SECONDS );
            return;
          }
          for ( ServiceReferenceListener listener : listeners ) {
            listener.serviceEvent( type, instance );
          }
        }
      }
    } );
  }

  public void registerPluginClass( Class clazz ) {
    if ( listeners.get( clazz ) != null ) {
      // Already tracking
      return;
    }
    int timeout = 40000;
    // Race condition. As OSGI is running in it's own thread, it may not be initialized yet. Wait for it.
    // TODO: replace this terrible synchronization crap
    while ( KarafHost.getInstance().isInitialized() == false/* && timeout > 0 */ ) {
      timeout -= 10;
      try {
        Thread.sleep( 10 );
      } catch ( InterruptedException e ) {
        e.printStackTrace();
      }
    }
    listeners.put( clazz, new ArrayList<OSGIServiceLifecycleListener>() );

    OSGIServiceTracker tracker = new OSGIServiceTracker( this, clazz );
    tracker.open();
    trackers.put( clazz, tracker );

  }

  public void serviceChanged( Class<?> cls, Event evt, ServiceReference serviceObject ) {
    Object instance = context.getService( serviceObject );
    instanceToReferenceMap.put( instance, serviceObject );

    // The beanfactory may not be registered yet. If not schedule a check every second until it is.
    BeanFactory factory = findOrCreateBeanFactoryFor( instance );
    if ( factory == null && evt != Event.STOP ) { // stopping services won't be able to find a beanfactory. Just skip
      ServiceReferenceListener.EVENT_TYPE type = null;
      switch( evt ) {
        case START:
          type = ServiceReferenceListener.EVENT_TYPE.STARTING;
          break;
        case MODIFY:
          type = ServiceReferenceListener.EVENT_TYPE.MODIFIED;
          break;
      }
      ScheduledFuture<?> timeHandle =
        scheduler.schedule( new DelayedServiceNotifier( cls, type, instance ), 2, TimeUnit.SECONDS );
      return;
    }
    for ( OSGIServiceLifecycleListener listener : listeners.get( cls ) ) {
      switch( evt ) {
        case START:
          listener.pluginAdded( instance );
          break;
        case STOP:
          listener.pluginRemoved( instance );
          break;
        case MODIFY:
          listener.pluginChanged( instance );
          break;
      }
    }
  }

  public void publishService( Class publishClazz, Object serviceObject, Dictionary props ) {
    if ( registeredServices.get( publishClazz ) != null ) {
      registeredServices.get( publishClazz ).unregister();
    }
    context.registerService( publishClazz.getName(), serviceObject, props );

  }

  public enum Event {
    START, STOP, MODIFY
  }

  class DelayedInstanceNotifier implements Runnable {
    private ServiceReferenceListener.EVENT_TYPE eventType;
    private Object serviceObject;

    public DelayedInstanceNotifier( ServiceReferenceListener.EVENT_TYPE eventType, Object serviceObject ) {
      this.eventType = eventType;
      this.serviceObject = serviceObject;
    }

    @Override
    public void run() {

      List<ServiceReferenceListener> listeners = instanceListeners.get( serviceObject );
      BeanFactory factory = findOrCreateBeanFactoryFor( serviceObject );
      if ( factory == null ) {
        ScheduledFuture<?> timeHandle =
          scheduler.schedule( new DelayedInstanceNotifier( eventType, serviceObject ), 2, TimeUnit.SECONDS );
      } else {
        for ( ServiceReferenceListener listener : listeners ) {
          listener.serviceEvent( eventType, serviceObject );
        }
      }
    }
  }

  class DelayedServiceNotifier implements Runnable {
    private Class<?> classToTrack;
    private ServiceReferenceListener.EVENT_TYPE eventType;
    private Object serviceObject;

    public DelayedServiceNotifier( Class<?> classToTrack, ServiceReferenceListener.EVENT_TYPE eventType,
                                   Object serviceObject ) {
      this.classToTrack = classToTrack;
      this.eventType = eventType;
      this.serviceObject = serviceObject;
    }

    @Override
    public void run() {

      BeanFactory factory = findOrCreateBeanFactoryFor( serviceObject );
      if ( factory == null ) {
        ScheduledFuture<?> timeHandle =
          scheduler.schedule( new DelayedServiceNotifier( classToTrack, eventType, serviceObject ), 2,
            TimeUnit.SECONDS );
      } else {
        for ( OSGIServiceLifecycleListener listener : listeners.get( classToTrack ) ) {
          switch( eventType ) {

            case STARTING:
              listener.pluginAdded( serviceObject );
              break;
            case STOPPING:
              listener.pluginRemoved( serviceObject );
              break;
            case MODIFIED:
              listener.pluginChanged( serviceObject );
              break;
          }
        }
      }
    }
  }
}
