package org.pentaho.di.osgi;

import org.pentaho.di.core.plugins.BasePluginType;
import org.pentaho.di.core.plugins.BasePluginTypeExposer;
import org.pentaho.di.core.plugins.PluginAnnotationType;
import org.pentaho.di.core.plugins.PluginMainClassType;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by nbaker on 2/9/15.
 */
public class AnnotationBasedOsgiPlugin extends OSGIPlugin {

  private BasePluginType basePluginType;
  private BasePluginTypeExposer exposer;
  private Object bean;
  private Annotation annotation;
  private Logger logger = LoggerFactory.getLogger( getClass() );


  public AnnotationBasedOsgiPlugin( Class<PluginTypeInterface> pluginTypeInterface, Object bean, String id, Map
    <Class, String> classToBeanMap, Object blueprintContainer )
    throws IllegalAccessException, InstantiationException {
    this.bean = bean;
    if ( !BasePluginType.class.isAssignableFrom( pluginTypeInterface ) ) {
      throw new IllegalArgumentException( "PluginTypeInterface must extend BasePluginType" );
    }
    boolean trySingleton = false;
    Constructor<PluginTypeInterface> constructor = null;
    try {
      constructor = pluginTypeInterface.getConstructor();
    } catch ( NoSuchMethodException e ) {
      logger.debug( "Error getting Constructor for BasePluginType of [ " + pluginTypeInterface.getName() + " ]" );
      trySingleton = true;
    }
    if ( constructor != null && constructor.isAccessible() ) {
      try {
        basePluginType = (BasePluginType) constructor.newInstance();
      } catch ( InvocationTargetException e ) {
        logger.debug( "Error calling Constructor for BasePluginType of [ " + pluginTypeInterface.getName() + " ]" );
        trySingleton = true;
      }
    } else {
      trySingleton = true;
    }
    if ( trySingleton ) {
      Method getInstance = null;
      try {
        getInstance = pluginTypeInterface.getMethod( "getInstance" );
        if ( getInstance != null ) {
          basePluginType = (BasePluginType) getInstance.invoke( pluginTypeInterface );
        }
      } catch ( NoSuchMethodException e ) {
        e.printStackTrace();
      } catch ( InvocationTargetException e ) {
        e.printStackTrace();
      }
    }

    Class<? extends Annotation> annotationCls = pluginTypeInterface.getAnnotation( PluginAnnotationType.class ).value();
    annotation = bean.getClass().getAnnotation( annotationCls );
    if ( basePluginType == null ) {
      throw new IllegalStateException( "Bean's PluginType could not be constructed, <pen:di-plugin> cannot be used." );
    }
    if ( annotation == null ) {
      throw new IllegalStateException( "Bean class does not have required PluginType annotation" );
    }
    super.setPluginTypeInterface( pluginTypeInterface );
    if ( classToBeanMap == null ) {
      classToBeanMap = new HashMap<>();
    }
    classToBeanMap.put( getMainType(), id );
    classToBeanMap.forEach( ( cls, beanId ) -> {
      super.addClassFactory( cls, new BlueprintBeanFactory( beanId, blueprintContainer ) );
    } );

    exposer = new BasePluginTypeExposer( basePluginType, bean );
  }

  @Override public String getDescription() {

    return exposer.extractDesc( annotation );
  }

  @Override public String getCategory() {
    return exposer.extractCategory( annotation );
  }

  @Override public String getID() {
    return exposer.extractID( annotation );
  }

  @Override public String getImageFile() {
    return exposer.extractImageFile( annotation );
  }

  @Override public Class<?> getMainType() {
    PluginMainClassType mainType = getPluginType().getAnnotation( PluginMainClassType.class );
    if ( mainType != null ) {
      return mainType.value();
    }
    return null;
  }

  @Override public String getName() {
    return exposer.extractName( annotation );
  }

  @Override public String getCasesUrl() {
    return exposer.extractCasesUrl( annotation );
  }

  @Override public String getDocumentationUrl() {
    return exposer.extractDocumentationUrl( annotation );
  }

}
