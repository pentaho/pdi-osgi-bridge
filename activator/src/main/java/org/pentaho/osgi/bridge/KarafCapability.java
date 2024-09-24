/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.osgi.bridge;

import com.google.common.util.concurrent.SettableFuture;
import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeaturesService;
import org.pentaho.capabilities.api.ICapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.Locale;
import java.util.concurrent.Future;

/**
 * Karaf Capability. This class wraps a Karaf Feature. Calls to install and uninstall a feature are made on the
 * FeatureService published to the OSGI Service Registry.
 *
 * Created by nbaker on 4/7/15.
 */
public class KarafCapability implements ICapability, Comparable<ICapability> {
  private FeaturesService featuresService;
  private Feature feature;
  private KarafCapabilityProvider manager;
  private Logger logger = LoggerFactory.getLogger( getClass() );

  public KarafCapability( FeaturesService featuresService, Feature feature, KarafCapabilityProvider manager ) {
    this.featuresService = featuresService;

    this.feature = feature;
    this.manager = manager;

  }

  @Override public String getId() {
    return feature.getName();
  }

  @Override public String getDescription( Locale locale ) {
    return feature.getDescription();
  }

  @Override public boolean isInstalled() {
    return featuresService.isInstalled( feature );
  }

  @Override public Future<Boolean> install() {
    SettableFuture<Boolean> installFuture = SettableFuture.<Boolean>create();
    try {
      manager.watchForInstall( feature, installFuture );
      featuresService.installFeature( feature, EnumSet.noneOf( FeaturesService.Option.class ) );
    } catch ( Exception e ) {
      logger.error( "Unknown error installing feature", e );
      installFuture.set( false );
      installFuture.setException( e );
    }
    return installFuture;
  }

  @Override public Future<Boolean> uninstall() {
    SettableFuture<Boolean> uninstallFuture = SettableFuture.<Boolean>create();
    try {
      manager.watchForUnInstall( feature, uninstallFuture );
      featuresService.uninstallFeature( feature.getName() );
    } catch ( Exception e ) {
      logger.error( "Unknown error uninstalling feature", e );
      uninstallFuture.set( false );
      uninstallFuture.setException( e );
    }
    return uninstallFuture;
  }

  @Override public URI getSourceUri() {
    try {
      return new URI( "karaf", feature.getDetails(), null );
    } catch ( URISyntaxException e ) {
      logger.error( "Error creating URI", e );
    }
    return null;
  }

  @Override public int compareTo( ICapability o ) {
    return this.getId().compareTo( o.getId() );
  }
}
