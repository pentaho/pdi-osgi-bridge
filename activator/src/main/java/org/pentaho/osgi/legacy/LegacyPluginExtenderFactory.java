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

package org.pentaho.osgi.legacy;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.List;

/**
 * Factory class to instantiate services implementing interfaces known to OSGi using classes from a legacy plugin
 */
public class LegacyPluginExtenderFactory {
  private final ClassLoader legacyBridgingClassloader;

  /**
   * Constructs the LegacyPluginExtenderFactory
   *
   * @param bundleContext the bundleContext
   * @param pluginType    the pluginType's canonical name
   * @param pluginId      the plugin id
   * @throws KettlePluginException
   */
  public LegacyPluginExtenderFactory( BundleContext bundleContext, String pluginType, String pluginId )
    throws KettlePluginException {
    ClassLoader parentClassloader = getPluginClassloader( pluginType, pluginId );
    if ( parentClassloader == null ) {
      throw new KettlePluginException(
        "Unable to get parent classloader for pluginType: " + pluginType + " and pluginId " + pluginId );
    } else {
      System.out.println( "Parent classloader: " + parentClassloader );
    }
    legacyBridgingClassloader = new LegacyPluginExtenderClassLoader( parentClassloader, bundleContext );
  }

  /**
   * Gets the classloader for the specified plugin, blocking until the plugin becomes available the feature watcher will
   * kill us after a while anyway
   *
   * @param pluginType the plugin type (Specified as a string so that we can get the classloader for plugin types OSGi
   *                   doesn't know about)
   * @param pluginId   the plugin id
   * @return
   * @throws KettlePluginException
   * @throws InterruptedException
   */
  private static ClassLoader getPluginClassloader( String pluginType, String pluginId )
    throws KettlePluginException {
    Class<? extends PluginTypeInterface> pluginTypeInterface = null;
    PluginRegistry pluginRegistry = PluginRegistry.getInstance();
    while ( true ) {
      synchronized ( pluginRegistry ) {
        if ( pluginTypeInterface == null ) {
          for ( Class<? extends PluginTypeInterface> potentialPluginTypeInterface : pluginRegistry.getPluginTypes() ) {
            if ( pluginType.equals( potentialPluginTypeInterface.getCanonicalName() ) ) {
              pluginTypeInterface = potentialPluginTypeInterface;
            }
          }
        }
        PluginInterface plugin = pluginRegistry.getPlugin( pluginTypeInterface, pluginId );
        if ( plugin != null ) {
          return pluginRegistry.getClassLoader( plugin );
        }
        try {
          pluginRegistry.wait();
        } catch ( InterruptedException e ) {
          throw new KettlePluginException( e );
        }
      }
    }
  }

  /**
   * Creates the object using a child-first classloader that uses local bundle bytecode first (so that it will be the
   * classloader for bundle classes created using this method which is necessary for correct loading of imports), the
   * bundleContext's classloader second, and the Kettle plugin's classloader third.
   * <p/>
   * This allows bundles to implement interfaces known only to OSGi using classes only known to Ketle plugins (or vice
   * versa).
   *
   * @param className the classname to instantiate
   * @return the instantiated class
   * @throws ClassNotFoundException
   * @throws InvocationTargetException
   * @throws InstantiationException
   * @throws KettlePluginException
   * @throws IllegalAccessException
   * @throws InterruptedException
   */
  public Object create( String className )
    throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException {
    return create( className, null );
  }

  /**
   * Creates the object using a child-first classloader that uses local bundle bytecode first (so that it will be the
   * classloader for bundle classes created using this method which is necessary for correct loading of imports), the
   * bundleContext's classloader second, and the Kettle plugin's classloader third.
   * <p/>
   * This allows bundles to implement interfaces known only to OSGi using classes only known to Ketle plugins (or vice
   * versa).
   *
   * @param className the classname to instantiate
   * @param arguments the arguments to use to instantiate the class
   * @return the instantiated class
   * @throws ClassNotFoundException
   * @throws InvocationTargetException
   * @throws InstantiationException
   * @throws KettlePluginException
   * @throws IllegalAccessException
   * @throws InterruptedException
   */
  public Object create( String className, List<Object> arguments )
    throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException {
    Class<?> clazz = Class.forName( className, true, legacyBridgingClassloader );
    if ( arguments == null || arguments.size() == 0 ) {
      return clazz.newInstance();
    }
    for ( Constructor<?> constructor : clazz.getConstructors() ) {
      Class<?>[] parameterTypes = constructor.getParameterTypes();
      if ( parameterTypes.length == arguments.size() ) {
        boolean match = true;
        for ( int i = 0; i < parameterTypes.length; i++ ) {
          Object o = arguments.get( i );
          if ( o != null && !parameterTypes[ i ].isInstance( o ) ) {
            match = false;
            break;
          }
        }
        if ( match ) {
          return constructor.newInstance( arguments.toArray() );
        }
      }
    }
    throw new InstantiationException(
      "Unable to find constructor for class " + className + " with arguments " + arguments );
  }

  /**
   * A classloader that will use load bytecode local to the bundle, the bundle's classloader as a second try, and the
   * parent classloader last
   */
  private static class LegacyPluginExtenderClassLoader extends ClassLoader {
    private final BundleWiring bundleWiring;
    private final PublicLoadResolveClassLoader bundleWiringClassloader;

    public LegacyPluginExtenderClassLoader( ClassLoader parentClassLoader, BundleContext bundleContext ) {
      super( parentClassLoader );
      this.bundleWiring = (BundleWiring) bundleContext.getBundle().adapt( BundleWiring.class );
      this.bundleWiringClassloader = new PublicLoadResolveClassLoader( bundleWiring.getClassLoader() );
    }

    @Override
    protected Class<?> findClass( String name ) throws ClassNotFoundException {
      int lastIndexOfDot = name.lastIndexOf( '.' );
      String translatedPath = "/" + name.substring( 0, lastIndexOfDot ).replace( '.', '/' );
      String translatedName = name.substring( lastIndexOfDot + 1 ) + ".class";
      List<URL> entries = bundleWiring.findEntries( translatedPath, translatedName, 0 );
      if ( entries.size() == 1 ) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
          IOUtils.copy( entries.get( 0 ).openStream(), byteArrayOutputStream );
        } catch ( IOException e ) {
          throw new ClassNotFoundException( "Unable to define class", e );
        }
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return defineClass( name, bytes, 0, bytes.length );
      }
      throw new ClassNotFoundException();
    }

    @Override
    public Class<?> loadClass( String name, boolean resolve ) throws ClassNotFoundException {
      Class<?> result = null;
      synchronized ( this ) {
        result = findLoadedClass( name );
      }
      if ( result == null ) {
        try {
          result = findClass( name );
        } catch ( Exception e ) {

        }
      }
      if ( result == null ) {
        try {
          return bundleWiringClassloader.loadClass( name, resolve );
        } catch ( Exception e ) {

        }
      }
      if ( result == null ) {
        return super.loadClass( name, resolve );
      }
      if ( resolve ) {
        resolveClass( result );
      }
      return result;
    }
  }

  /**
   * Trivial classloader subclass that lets us call loadClass with a resolve parameter
   */
  private static class PublicLoadResolveClassLoader extends ClassLoader {
    public PublicLoadResolveClassLoader( ClassLoader parent ) {
      super( parent );
    }

    @Override
    public Class<?> loadClass( String name, boolean resolve ) throws ClassNotFoundException {
      return super.loadClass( name, resolve );
    }
  }
}
