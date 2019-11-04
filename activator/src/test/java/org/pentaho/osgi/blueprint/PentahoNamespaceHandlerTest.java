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

package org.pentaho.osgi.blueprint;

import org.apache.aries.blueprint.ComponentDefinitionRegistry;
import org.apache.aries.blueprint.ParserContext;
import org.apache.aries.blueprint.mutable.MutableBeanMetadata;
import org.apache.aries.blueprint.mutable.MutableMapMetadata;
import org.apache.aries.blueprint.mutable.MutableRefMetadata;
import org.apache.aries.blueprint.mutable.MutableServiceMetadata;
import org.apache.aries.blueprint.mutable.MutableValueMetadata;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.osgi.service.blueprint.reflect.Metadata;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import javax.xml.soap.Node;
import java.net.URL;
import java.util.Set;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class PentahoNamespaceHandlerTest {

  BundleContext bundleContext;
  PentahoNamespaceHandler pentahoNamespaceHandler;

  @Before
  public void setUp() throws Exception {
    bundleContext = mock( BundleContext.class );
  }

  @Test
  public void testGetSchemaLocation() throws Exception {
    pentahoNamespaceHandler = new PentahoNamespaceHandler( bundleContext );

    URL url = pentahoNamespaceHandler.getSchemaLocation( PentahoNamespaceHandler.PENTAHO_BLUEPRINT_SCHEMA );

    assertNotNull( url );
    assert( url.getPath().contains( "pentaho-blueprint" ) );
  }

  @Test
  public void testGetOtherSchemaLocation() throws Exception {
    pentahoNamespaceHandler = new PentahoNamespaceHandler( bundleContext );

    URL url = pentahoNamespaceHandler.getSchemaLocation( "test" );

    assertNull( url );
  }

  @Test
  public void testGetManagedClasses() throws Exception {
    pentahoNamespaceHandler = new PentahoNamespaceHandler( bundleContext );

    Set managedClasses = pentahoNamespaceHandler.getManagedClasses();

    // managedClasses not implemented
    assert( managedClasses == null);
  }

  @Test
  public void testParse() throws Exception {
    pentahoNamespaceHandler = new PentahoNamespaceHandler( bundleContext );
    Element mockElement = mock( Element.class );
    ParserContext parserContext = mock( ParserContext.class );

    Metadata metadata = pentahoNamespaceHandler.parse( mockElement, parserContext );

    // metadata not implemented
    assert( metadata == null );
  }

  @Test
  public void testDecorate() throws Exception {
    pentahoNamespaceHandler = new PentahoNamespaceHandler( bundleContext );
    Node node = mock( Node.class );
    Node namedNode = mock( Node.class );
    NamedNodeMap namedNodeMap = mock( NamedNodeMap.class );

    Node node1 = mock( Node.class );
    Node node2 = mock( Node.class );
    Node node3 = mock( Node.class );

    NamedNodeMap nodeListNamedNodeMap = mock( NamedNodeMap.class );
    when( node1.getAttributes() ).thenReturn( nodeListNamedNodeMap );
    when( node2.getAttributes() ).thenReturn( nodeListNamedNodeMap );
    when( node3.getAttributes() ).thenReturn( nodeListNamedNodeMap );
    when( nodeListNamedNodeMap.getNamedItem( anyString() ) ).thenReturn( node );

    NodeList nodeList = mock( NodeList.class );
    when( nodeList.getLength() ).thenReturn( 3 );
    when( nodeList.item( 0 ) ).thenReturn( node1 );
    when( nodeList.item( 1 ) ).thenReturn( node2 );
    when( nodeList.item( 2 ) ).thenReturn( node3 );

    when( node.getNodeName() ).thenReturn( "<pen:di-plugin" );
    when( node.getAttributes() ).thenReturn( namedNodeMap );
    when( namedNodeMap.getNamedItem( PentahoNamespaceHandler.TYPE ) ).thenReturn( namedNode );
    when( namedNode.getTextContent() ).thenReturn( "test" );

    ComponentMetadata componentMetadata = mock( ComponentMetadata.class );
    ComponentDefinitionRegistry componentDefinitionRegistry = mock( ComponentDefinitionRegistry.class );
    ParserContext parserContext = mock( ParserContext.class );
    MutableBeanMetadata mutableBeanMetadata = mock( MutableBeanMetadata.class );
    MutableValueMetadata mutableValueMetadata = mock( MutableValueMetadata.class );
    MutableRefMetadata mutableRefMetadata = mock( MutableRefMetadata.class );
    MutableMapMetadata mutableMapMetadata = mock( MutableMapMetadata.class );
    MutableServiceMetadata mutableServiceMetadata = mock( MutableServiceMetadata.class );

    when( parserContext.createMetadata( MutableBeanMetadata.class ) ).thenReturn( mutableBeanMetadata );
    when( parserContext.createMetadata( MutableValueMetadata.class ) ).thenReturn( mutableValueMetadata );
    when( parserContext.createMetadata( MutableRefMetadata.class ) ).thenReturn( mutableRefMetadata );
    when( parserContext.createMetadata( MutableMapMetadata.class ) ).thenReturn( mutableMapMetadata );
    when( parserContext.createMetadata( MutableServiceMetadata.class  ) ).thenReturn( mutableServiceMetadata );
    when( parserContext.getComponentDefinitionRegistry() ).thenReturn( componentDefinitionRegistry );
    when( node.getChildNodes() ).thenReturn( nodeList );

    ComponentMetadata updatedComponentMetadata = pentahoNamespaceHandler.decorate( node, componentMetadata, parserContext );

    assertNotNull( updatedComponentMetadata );
    assert( updatedComponentMetadata.equals( componentMetadata ) );
    verify( node ).getAttributes();
    verify( node ).getChildNodes();
    verify( parserContext, times( 2 ) ).getComponentDefinitionRegistry();
    verify( parserContext, times( 14 ) ).createMetadata( (Class) anyObject() );
  }
}