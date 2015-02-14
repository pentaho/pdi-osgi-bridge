package org.pentaho.osgi.blueprint.test;

import org.apache.aries.blueprint.NamespaceHandler;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.pentaho.di.core.plugins.KettleLifecyclePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.osgi.AnnotationBasedOsgiPlugin;
import org.pentaho.di.osgi.BundleClassloaderWrapper;
import org.pentaho.di.osgi.PentahoNamespaceHandler;
import org.pentaho.di.osgi.OSGIPlugin;
import org.pentaho.di.osgi.OSGIPluginTracker;
import org.pentaho.di.osgi.OSGIPluginType;
import org.pentaho.di.osgi.PentahoNamespaceActivator;
import org.pentaho.di.osgi.ServiceReferenceListener;
import org.pentaho.di.osgi.service.lifecycle.LifecycleEvent;
import org.pentaho.di.osgi.service.lifecycle.OSGIServiceLifecycleListener;
import org.pentaho.di.osgi.service.lifecycle.PluginRegistryOSGIServiceLifecycleListener;
import org.pentaho.di.osgi.service.listener.BundleContextServiceListener;
import org.pentaho.di.osgi.service.notifier.DelayedInstanceNotifier;
import org.pentaho.di.osgi.service.notifier.DelayedInstanceNotifierFactory;
import org.pentaho.di.osgi.service.notifier.DelayedServiceNotifier;
import org.pentaho.di.osgi.service.tracker.OSGIServiceTracker;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;
import static org.ops4j.pax.tinybundles.core.TinyBundles.withBnd;

/**
 * Created by nbaker on 2/11/15.
 */

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class TestKettlePlugin {


    @Configuration
    public Option[] config() throws FileNotFoundException {

      InputStream inp = bundle()
          .add( PentahoNamespaceActivator.class )
          .add( AnnotationBasedOsgiPlugin.class )
          .add( OSGIPluginTracker.class )
          .add( BundleContextServiceListener.class )
          .add( OSGIServiceTracker.class )
          .add( OSGIPluginType.class )
          .add( ServiceReferenceListener.class )
          .add( OSGIPlugin.class )
          .add( PentahoNamespaceHandler.class )
          .add( BundleClassloaderWrapper.class )
          .add( LifecycleEvent.class )
          .add( OSGIServiceLifecycleListener.class )
          .add( PluginRegistryOSGIServiceLifecycleListener.class )
          .add( DelayedInstanceNotifier.class )
          .add( DelayedInstanceNotifierFactory.class )
          .add( DelayedServiceNotifier.class )
          .add( "pentaho-blueprint.xsd", getClass().getResourceAsStream( "/pentaho-blueprint.xsd" ) )
          .set( Constants.BUNDLE_SYMBOLICNAME, "Test_Bundle" )
          .set( Constants.EXPORT_PACKAGE, "*" )
          .set( Constants.IMPORT_PACKAGE, "*" )
          .set( Constants.DYNAMICIMPORT_PACKAGE, "*" )
          .set( Constants.BUNDLE_ACTIVATOR, PentahoNamespaceActivator.class.getName() )
          .build( withBnd() );

      InputStream inp2 = bundle()
          .add( TestObject.class )
          .set( Constants.BUNDLE_SYMBOLICNAME, "Test_Bundle2" )
          .set( Constants.EXPORT_PACKAGE, "*" )
          .set( Constants.IMPORT_PACKAGE, "org.pentaho.di.core.plugins,org.pentaho.di.osgi,*" )
          .set( Constants.DYNAMICIMPORT_PACKAGE, "*" )
          .add( "/OSGI-INF/blueprint/beans.xml",
              new FileInputStream( new File( "src/test/resources/blueprint/beans.xml" ) ) )
          .build( withBnd() );





      MavenArtifactUrlReference karafUrl = maven()
          .groupId( "org.apache.karaf" )
          .artifactId( "apache-karaf" )
          .version( "3.0.2" )
          .type( "tar.gz" );

      final String projectVersion = "6.0-SNAPSHOT";
      return options(

          bootClasspathLibraries( "mvn:pentaho-kettle/kettle-core/" + projectVersion,
              "mvn:pentaho-kettle/kettle-engine/" + projectVersion
              , "mvn:pentaho/pentaho-osgi-utils-api/" + projectVersion
              , "mvn:commons-vfs/commons-vfs/1.0",
              "mvn:net.sf.scannotation/scannotation/1.0.2" ),
//          vmOption( "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005" ),
          systemPackages( "org.pentaho.di.i18n", "org.pentaho.osgi.api", "org.pentaho.di.core.plugins",
              "org.pentaho.di.core.exception", "org.apache.commons.vfs", "org.scannotation",
              "org.pentaho.di.core.annotations", "org.pentaho.di.core.lifecycle" ),

          KarafDistributionOption.karafDistributionConfiguration()
              .frameworkUrl( karafUrl )
              .unpackDirectory( new File( "target/exam" ) )
              .useDeployFolder( false ),
          junitBundles(),
          mavenBundle( "commons-beanutils", "commons-beanutils", "1.9.2" ),
          mavenBundle( "pentaho", "pentaho-monitoring-plugin", projectVersion ),
          mavenBundle( "commons-collections", "commons-collections", "3.2.1" ),
          provision( inp )
          , provision( inp2 )

      );
    }


    @Inject
    private BundleContext bundleContext;

    private Bundle getBundleBySymbolicName( String name ){
      Bundle[] bundles = bundleContext.getBundles();
      for (Bundle bundle : bundles) {
        if(bundle.getSymbolicName().equals(name)){
          return bundle;
        }
      }
      return null;
    }


    @Test
    public void testNamespaceHandler() throws BundleException, InvalidSyntaxException {
      Collection<ServiceReference<NamespaceHandler>> blueprintContainerServiceReference =
          bundleContext.getServiceReferences( NamespaceHandler.class,
              "(osgi.service.blueprint.namespace=http://www.pentaho.com/xml/schemas/pentaho-blueprint)" );
      assertNotNull( blueprintContainerServiceReference );
      assertTrue( blueprintContainerServiceReference.size() > 0 );

      ServiceReference<PluginInterface> pluginInterfaceServiceReference = bundleContext.getServiceReference( PluginInterface.class );
      assertNotNull( pluginInterfaceServiceReference );
      PluginInterface plugin = bundleContext.getService( pluginInterfaceServiceReference );
      Assert.assertEquals( KettleLifecyclePluginType.class, plugin.getPluginType() );
      Assert.assertEquals( "testLifeCycleListener", plugin.getIds()[ 0 ] );
    }
  }