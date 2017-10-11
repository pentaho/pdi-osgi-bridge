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

import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeaturesService;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.capabilities.api.ICapability;

import java.net.URI;
import java.util.Locale;
import java.util.concurrent.Future;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KarafCapabilityTest {

  private static final String TEST_CAPABILITY_DESCRIPTION = "test description";
  private static final String TEST_CAPABILITY_NAME = "test";
  private static final String TEST_CAPABILITY_DETAILS = "test details";

  KarafCapability karafCapability;
  FeaturesService featuresService;
  Feature feature;
  KarafCapabilityProvider karafCapabilityProvider;

  @Before
  public void setUp() throws Exception {
    featuresService = mock( FeaturesService.class );
    feature = mock( Feature.class );
    when( feature.getName() ).thenReturn( TEST_CAPABILITY_NAME );
    when( feature.getDescription() ).thenReturn( TEST_CAPABILITY_DESCRIPTION );
    when( feature.getDetails() ).thenReturn( TEST_CAPABILITY_DETAILS );
    karafCapabilityProvider = mock( KarafCapabilityProvider.class );
  }

  @Test
  public void testGetId() throws Exception {
    karafCapability = new KarafCapability( featuresService, feature, karafCapabilityProvider );
    String capabilityId = karafCapability.getId();
    assert( capabilityId.equals( TEST_CAPABILITY_NAME ) );
  }

  @Test
  public void testGetDescription() throws Exception {
    karafCapability = new KarafCapability( featuresService, feature, karafCapabilityProvider );
    String capabilityDescription = karafCapability.getDescription( Locale.getDefault() );
    assert( capabilityDescription.equals( TEST_CAPABILITY_DESCRIPTION ) );
  }

  @Test
  public void testIsInstalled() throws Exception {
    karafCapability = new KarafCapability( featuresService, feature, karafCapabilityProvider );
    boolean isInstalled = karafCapability.isInstalled();
    assert( isInstalled == Boolean.FALSE );
  }

  @Test
  public void testInstall() throws Exception {
    karafCapability = new KarafCapability( featuresService, feature, karafCapabilityProvider );
    Future install = karafCapability.install();
    assert( install != null );
  }

  @Test
  public void testUninstall() throws Exception {
    karafCapability = new KarafCapability( featuresService, feature, karafCapabilityProvider );
    Future uninstall = karafCapability.uninstall();
    assert( uninstall != null );
  }

  @Test
  public void testGetSourceUri() throws Exception {
    karafCapability = new KarafCapability( featuresService, feature, karafCapabilityProvider );
    URI uri = karafCapability.getSourceUri();
    assert( uri != null );
  }

  @Test
  public void testCompareTo() throws Exception {
    karafCapability = new KarafCapability( featuresService, feature, karafCapabilityProvider );
    ICapability capability = mock( ICapability.class );
    when( capability.getId() ).thenReturn( TEST_CAPABILITY_NAME );
    int isComparable = karafCapability.compareTo( capability );
    assert( isComparable == 0 );
  }
}