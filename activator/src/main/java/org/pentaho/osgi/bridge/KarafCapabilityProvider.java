package org.pentaho.osgi.bridge;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeatureEvent;
import org.apache.karaf.features.FeaturesListener;
import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.features.RepositoryEvent;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.pentaho.capabilities.api.ICapability;
import org.pentaho.capabilities.api.ICapabilityProvider;
import org.pentaho.capabilities.impl.DefaultCapabilityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Karaf Capability Provider. This class exposes Karaf Features as Pentaho ICapabilities
 *
 * Created by nbaker on 4/6/15.
 */
public class KarafCapabilityProvider extends ServiceTracker<FeaturesService, FeaturesService> implements ICapabilityProvider,
    FeaturesListener {


  public static final String KARAF = "Karaf";
  private BundleContext bundleContext;
    private FeaturesService featuresService;
  private AtomicBoolean initialized = new AtomicBoolean( false );
  private Logger logger = LoggerFactory.getLogger(getClass());
  private static final boolean UNINSTALL = false;
  private static final boolean INSTALL = true;

  Cache<String, InstallFuture> futures = CacheBuilder.newBuilder()
      .concurrencyLevel( 4 )
      .maximumSize( 1024 )
      .expireAfterWrite( 20, TimeUnit.SECONDS )
      .removalListener( new RemovalListener<String, InstallFuture>() {
        @Override public void onRemoval( RemovalNotification<String, InstallFuture> removalNotification ) {
          switch( removalNotification.getCause() ) {

            case EXPLICIT:
              break;
            case REPLACED:
            case COLLECTED:
            case EXPIRED:
            case SIZE:
              SettableFuture<Boolean> value = removalNotification.getValue().future;
              if( value != null ){ // May have been GCed
                value.set( false );
              }
              break;
          }
        }
      } )
      .build();

  private class InstallFuture{
    SettableFuture<Boolean> future;
    boolean install;

    public InstallFuture( SettableFuture<Boolean> future, boolean install ) {
      this.future = future;
      this.install = install;
    }
  }

  public KarafCapabilityProvider( BundleContext bundleContext ) {
    super(bundleContext, FeaturesService.class, null);
    this.bundleContext = bundleContext;
    this.bundleContext.registerService( FeaturesListener.class, this, null );
  }

  @Override public FeaturesService addingService( ServiceReference<FeaturesService> reference ) {
    this.featuresService = bundleContext.getService( reference );
    if(! initialized.getAndSet( true )) {
      DefaultCapabilityManager.getInstance().registerCapabilityProvider( this );
    }
    return super.addingService( reference );
  }

  @Override public String getId() {
    return KARAF;
  }

  @Override public Set<String> listCapabilities() {
    Set<String> ids = new HashSet<>();
    try {
      for ( Feature feature : featuresService.listFeatures() ) {
        ids.add( feature.getName() );
      }
    } catch ( Exception e ) {
      logger.error( "Unknown error trying to retrieve available Karaf features", e );
      return ids;
    }
    return ids;
  }

  @Override public ICapability getCapabilityById( String id ) {
    try {
      return new KarafCapability( featuresService, featuresService.getFeature( id ), this );
    } catch ( Exception e ) {
      logger.error( "Unknown error retrieving feature: "+id, e );
    }
    return null;
  }

  @Override public Set<ICapability> getAllCapabilities() {
    Set<ICapability> capabilities = new HashSet<ICapability>();
    try {
      for ( Feature feature : featuresService.listFeatures() ) {
        ICapability capabilityById = this.getCapabilityById( feature.getName() );
        if( capabilityById != null ) {
          capabilities.add( capabilityById );
        }
      }
    } catch ( Exception e ) {
      logger.error( "Unknown error trying to retrieve available Karaf features", e );
      return capabilities;
    }
    return capabilities;
  }

  public void watchForInstall( Feature feature, SettableFuture<Boolean> future ){
    futures.put( feature.getId(), new InstallFuture(future, INSTALL));
  }

  public void watchForUnInstall( Feature feature, SettableFuture<Boolean> uninstallFuture ) {
    futures.put( feature.getId(), new InstallFuture(uninstallFuture, UNINSTALL));
  }

  @Override public void featureEvent( FeatureEvent featureEvent ) {
    String feature = featureEvent.getFeature().getId();
    InstallFuture installFutureWrapper = futures.getIfPresent( feature );
    if( installFutureWrapper == null ){
      return;
    }
    switch( featureEvent.getType() ) {
      case FeatureInstalled:
        SettableFuture<Boolean> future = installFutureWrapper.future;
        if( future != null ){
          futures.invalidate( feature );
          future.set( installFutureWrapper.install );
        }
        break;
      case FeatureUninstalled:
        future = installFutureWrapper.future;
        if( future != null ){
          futures.invalidate( feature );
          future.set( ! installFutureWrapper.install );
        }
        break;
    }

  }

  @Override public void repositoryEvent( RepositoryEvent repositoryEvent ) {
    // ignored
  }
}
