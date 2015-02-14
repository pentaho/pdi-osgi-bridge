package org.pentaho.osgi.blueprint;


import org.apache.aries.blueprint.NamespaceHandler;
import org.apache.aries.blueprint.ParserContext;
import org.apache.aries.blueprint.mutable.MutableBeanMetadata;
import org.apache.aries.blueprint.mutable.MutableRefMetadata;
import org.apache.aries.blueprint.mutable.MutableServiceMetadata;
import org.apache.aries.blueprint.mutable.MutableValueMetadata;
import org.osgi.framework.BundleContext;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.osgi.service.blueprint.reflect.Metadata;
import org.osgi.service.blueprint.reflect.ServiceMetadata;
import org.pentaho.di.osgi.AnnotationBasedOsgiPlugin;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.net.URL;
import java.util.Set;

/**
 * A Custom Blueprint (Aries) NamespaceHandler. This class is responsible for parsing all namespaced elements from the
 * http://www.pentaho.com/xml/schemas/pentaho-blueprint namespace.
 * <p/>
 * Created by nbaker on 2/10/15.
 */
public class PentahoNamespaceHandler implements NamespaceHandler {
  public static final String DI_PLUGIN = "di-plugin";
  public static final String TYPE = "type";
  public static final String AUTO_PDI_PLUGIN = "auto_pdi_plugin_";
  private BundleContext bundleContext;

  public PentahoNamespaceHandler( BundleContext bundleContext ) {
    this.bundleContext = bundleContext;
  }

  @Override public URL getSchemaLocation( String s ) {
    return getClass().getResource( "/pentaho-blueprint.xsd" );
  }

  @Override public Set<Class> getManagedClasses() {
    return null;
  }

  @Override public Metadata parse( Element element, ParserContext parserContext ) {
    return null;
  }

  @Override
  public ComponentMetadata decorate( Node node, ComponentMetadata componentMetadata, ParserContext parserContext ) {
    if ( DI_PLUGIN.equals( node.getNodeName() ) ) {     // <pen:di-plugin type="..."/>

      MutableBeanMetadata pluginBeanMetadata = createKettlePluginBean( node, componentMetadata, parserContext );
      MutableServiceMetadata serviceMetadata = createServiceMeta( componentMetadata, parserContext, pluginBeanMetadata );


      // Register with Blueprint Container
      parserContext.getComponentDefinitionRegistry().registerComponentDefinition( pluginBeanMetadata );
      parserContext.getComponentDefinitionRegistry().registerComponentDefinition( serviceMetadata );


    }
    // We return the original component.
    return componentMetadata;
  }

  /**
   * Constructs a Service element publishing the Kettle Plugin to the OSGI Service Registry. The effect is as if the
   * following was written in the Blueprint file:
   *
   * <service auto-export="interfaces" ref="kettlePluginBean"/>
   *
   * @param componentMetadata
   * @param parserContext
   * @param pluginBeanMetadata
   * @return
   */
  private MutableServiceMetadata createServiceMeta( ComponentMetadata componentMetadata, ParserContext parserContext,
                                                    MutableBeanMetadata pluginBeanMetadata ) {
    // Create Service Meta
    MutableServiceMetadata serviceMetadata = parserContext.createMetadata( MutableServiceMetadata.class );

    // Create Reference Meta
    MutableRefMetadata mutableRefMetadata = parserContext.createMetadata( MutableRefMetadata.class );
    mutableRefMetadata.setComponentId( pluginBeanMetadata.getId() );

    serviceMetadata.setServiceComponent( mutableRefMetadata );
    serviceMetadata.setAutoExport( ServiceMetadata.AUTO_EXPORT_INTERFACES );
    serviceMetadata.setId( "auto_pdi_plugin_service_" + componentMetadata.getId() );
    return serviceMetadata;

  }

  /**
   * Constructs a new Bean of type AnnotationBasedOsgiPlugin which will extract values from the target bean. The effect
   * is as if the user had written the following bean in their blueprint container
   *
   * <bean class="org.pentaho.di.osgi.AnnotationBasedOsgiPlugin">
   *   <argument>org.pentaho.di.core.plugins.KettleLifecyclePluginType</argument>
   *   <argument ref="targetBeanID"/>
   *   <argument>targetBeanID</argument>
   * </bean>
   *
   * @param node
   * @param componentMetadata
   * @param parserContext
   */
  private MutableBeanMetadata createKettlePluginBean( Node node, ComponentMetadata componentMetadata,
                                                   ParserContext parserContext ) {
    // Extract the kettle plugin type from the attribute
    String kettlePluginType = node.getAttributes().getNamedItem( TYPE ).getTextContent();

    // Construct a new Bean of type AnnotationBasedOsgiPlugin which will extract values from the target bean
    MutableBeanMetadata pluginBeanMetadata = parserContext.createMetadata( MutableBeanMetadata.class );

    pluginBeanMetadata.setClassName( AnnotationBasedOsgiPlugin.class.getName() );

    // Create Arguments

    // KettlePluginType
    MutableValueMetadata kettlePluginTypeMeta = parserContext.createMetadata( MutableValueMetadata.class );
    kettlePluginTypeMeta.setStringValue( kettlePluginType );

    // Reference to Bean
    MutableRefMetadata beanReferenceMeta = parserContext.createMetadata( MutableRefMetadata.class );
    beanReferenceMeta.setComponentId( componentMetadata.getId() );

    // name of Bean
    MutableValueMetadata beanNameMeta = parserContext.createMetadata( MutableValueMetadata.class );
    beanNameMeta.setStringValue( componentMetadata.getId() );

    // Set Arguments
    pluginBeanMetadata.addArgument( kettlePluginTypeMeta, null, 0 );
    pluginBeanMetadata.addArgument( beanReferenceMeta, null, 1 );
    pluginBeanMetadata.addArgument( beanNameMeta, null, 2 );

    // Give it an ID (required)
    pluginBeanMetadata.setId( AUTO_PDI_PLUGIN + componentMetadata.getId() );

    return pluginBeanMetadata;
  }
}
