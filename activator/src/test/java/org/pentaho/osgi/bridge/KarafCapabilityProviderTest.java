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

package org.pentaho.osgi.bridge;

import com.google.common.util.concurrent.SettableFuture;
import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeatureEvent;
import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.features.RepositoryEvent;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.pentaho.capabilities.api.ICapability;

import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KarafCapabilityProviderTest {

  private static final String TEST_CAPABILITY_ID = "test";
  private static final java.lang.String TEST_FEATURE_ID = "test";

  KarafCapabilityProvider karafCapabilityProvider;
  BundleContext bundleContext;
  FeaturesService featuresService;

  @Before
  public void setUp() throws Exception {
    Feature feature = mock( Feature.class );
    when( feature.getName() ).thenReturn( TEST_FEATURE_ID );
    Feature[] featureList = {feature};
    bundleContext = mock( BundleContext.class );
    featuresService = mock( FeaturesService.class );
    when( featuresService.listFeatures() ).thenReturn( featureList );
    when( featuresService.getFeature( anyString() ) ).thenReturn( feature );
    when( bundleContext.getService( (ServiceReference) anyObject() ) ).thenReturn( featuresService );
  }

  @Test
  public void testAddingService() throws Exception {
    karafCapabilityProvider = new KarafCapabilityProvider( bundleContext );
    ServiceReference serviceReference = mock( ServiceReference.class );
    assertEquals( featuresService, karafCapabilityProvider.addingService( serviceReference ) );
  }

  @Test
  public void testGetId() throws Exception {
    karafCapabilityProvider = new KarafCapabilityProvider( bundleContext );
    String id = karafCapabilityProvider.getId();
    assertNotNull( id );
    assert( id.equals( "Karaf" ) );
  }

  @Test
  public void testListCapabilities() throws Exception {
    karafCapabilityProvider = new KarafCapabilityProvider( bundleContext );
    ServiceReference serviceReference = mock( ServiceReference.class );
    karafCapabilityProvider.addingService( serviceReference );
    Set<String> capabilities = karafCapabilityProvider.listCapabilities();
    assertNotNull( capabilities );
    assert( capabilities.size() > 0 );
  }

  @Test
  public void testGetCapabilityById() throws Exception {
    Feature feature = mock( Feature.class );
    Feature[] featureList = {feature};
    when( featuresService.getFeature( anyString() )).thenReturn( feature );
    karafCapabilityProvider = new KarafCapabilityProvider( bundleContext );
    ServiceReference serviceReference = mock( ServiceReference.class );
    karafCapabilityProvider.addingService( serviceReference );
    ICapability capability = karafCapabilityProvider.getCapabilityById( TEST_CAPABILITY_ID );
    assertNotNull( capability );
    assert( capability.getClass().equals( KarafCapability.class ) );
  }

  @Test
  public void testCapabilityExist() throws Exception {
    Feature feature = mock( Feature.class );
    when( featuresService.getFeature( anyString() )).thenReturn( feature );
    karafCapabilityProvider = new KarafCapabilityProvider( bundleContext );
    ServiceReference serviceReference = mock( ServiceReference.class );
    karafCapabilityProvider.addingService( serviceReference );

    boolean exists = karafCapabilityProvider.capabilityExist( TEST_CAPABILITY_ID );
    assertTrue( exists );

    when( featuresService.getFeature( anyString() )).thenReturn( null );
    boolean doesNotExist = karafCapabilityProvider.capabilityExist( "bad" );
    assertFalse( doesNotExist );
  }

  @Test
  public void testGetAllCapabilities() throws Exception {
    ServiceReference serviceReference = mock( ServiceReference.class );
    karafCapabilityProvider = new KarafCapabilityProvider( bundleContext );
    karafCapabilityProvider.addingService( serviceReference );
    Set<ICapability> capabilities = karafCapabilityProvider.getAllCapabilities();
    assertNotNull( capabilities );
    assert( capabilities.size() >  0);
    assert( capabilities.iterator().next().getClass().equals( KarafCapability.class ) );
  }

  @Test
  public void testWatchForInstall() throws Exception {
    Feature feature = mock( Feature.class );
    when( feature.getId() ).thenReturn( TEST_FEATURE_ID );
    SettableFuture settableFuture = SettableFuture.<Boolean> create();
    karafCapabilityProvider = new KarafCapabilityProvider( bundleContext );
    karafCapabilityProvider.watchForInstall( feature, settableFuture );
    verify( feature ).getId();
  }

  @Test
  public void testWatchForUnInstall() throws Exception {
    karafCapabilityProvider = new KarafCapabilityProvider( bundleContext );
    Feature feature = mock( Feature.class );
    when( feature.getId() ).thenReturn( TEST_FEATURE_ID );
    SettableFuture settableFuture = SettableFuture.<Boolean> create();
    karafCapabilityProvider.watchForUnInstall( feature, settableFuture );
    verify( feature ).getId();
  }

  @Test
  public void testFeatureEvent() throws Exception {
    FeaturesService featuresService = mock( FeaturesService.class );
    when( bundleContext.getService( (ServiceReference) anyObject() ) ).thenReturn( featuresService );
    karafCapabilityProvider = new KarafCapabilityProvider( bundleContext );
    ServiceReference serviceReference = mock( ServiceReference.class );
    karafCapabilityProvider.addingService( serviceReference );

    karafCapabilityProvider = new KarafCapabilityProvider( bundleContext );
    Feature feature = mock( Feature.class );
    when( feature.getId() ).thenReturn( TEST_FEATURE_ID );
    FeatureEvent featureEvent = mock( FeatureEvent.class );
    when( featureEvent.getFeature() ).thenReturn( feature );
    when( featureEvent.getType() ).thenReturn( FeatureEvent.EventType.FeatureInstalled );
    SettableFuture settableFuture = SettableFuture.<Boolean> create();
    karafCapabilityProvider.watchForInstall( feature, settableFuture );
    karafCapabilityProvider.featureEvent( featureEvent );
    verify( featureEvent ).getFeature();
    verify( featureEvent ).getType();

  }

  @Test
  public void testFeatureEventUninstalled() throws Exception {
    FeaturesService featuresService = mock( FeaturesService.class );
    when( bundleContext.getService( (ServiceReference) anyObject() ) ).thenReturn( featuresService );
    karafCapabilityProvider = new KarafCapabilityProvider( bundleContext );
    ServiceReference serviceReference = mock( ServiceReference.class );
    karafCapabilityProvider.addingService( serviceReference );

    karafCapabilityProvider = new KarafCapabilityProvider( bundleContext );
    Feature feature = mock( Feature.class );
    when( feature.getId() ).thenReturn( TEST_FEATURE_ID );
    FeatureEvent featureEvent = mock( FeatureEvent.class );
    when( featureEvent.getFeature() ).thenReturn( feature );
    when( featureEvent.getType() ).thenReturn( FeatureEvent.EventType.FeatureUninstalled );
    SettableFuture settableFuture = SettableFuture.<Boolean> create();
    karafCapabilityProvider.watchForUnInstall( feature, settableFuture );
    karafCapabilityProvider.featureEvent( featureEvent );

    verify( featureEvent ).getFeature();
    verify( featureEvent ).getType();
  }

  @Test
  public void testRepositoryEvent() throws Exception {
    karafCapabilityProvider = new KarafCapabilityProvider( bundleContext );
    RepositoryEvent repositoryEvent = mock( RepositoryEvent.class );

    // ignored
    karafCapabilityProvider.repositoryEvent( repositoryEvent );
  }
}