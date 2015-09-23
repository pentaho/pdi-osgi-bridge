package org.pentaho.di.osgi;

import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.pentaho.osgi.api.IKarafBlueprintWatcher;
import org.pentaho.osgi.api.IKarafFeatureWatcher;
import org.pentaho.platform.servicecoordination.api.IPhasedLifecycleEvent;
import org.pentaho.platform.servicecoordination.impl.BaseCountdownLatchLifecycleManager;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by nbaker on 9/23/15.
 */
public class KarafLifecycleListenerTest {

  @Test
  public void testSatisfied() throws Exception {

    final BaseCountdownLatchLifecycleManager<KettleLifecycleEvent> lifecycleManager = new BaseCountdownLatchLifecycleManager<KettleLifecycleEvent>() {
      @Override protected KettleLifecycleEvent getNotificationObject() {
        return KettleLifecycleEvent.INIT;
      }
    };

    KarafLifecycleListener listener = new KarafLifecycleListener( 500 );
    lifecycleManager.addLifecycleListener( listener );
    lifecycleManager.advance();
    assertTrue( lifecycleManager.getPhase() == 1 );

    Thread t = new Thread( new Runnable() {
      @Override public void run() {
        try {
          lifecycleManager.advance();
        } catch ( InterruptedException e ) {
          fail( "error in manager" );
          e.printStackTrace();
        }
      }
    });
    t.start();

    int fatalCount = 0;
    do {
      Thread.sleep( 10 );
    } while ( t.getState() != Thread.State.WAITING && ++fatalCount < 10 );
    assertTrue( t.getState() == Thread.State.WAITING );
    assertEquals( 1, lifecycleManager.getPhase() ); // still at 1 because the first is blocked


    // Now lets satisfy the requirements, the listener should release now
    IKarafFeatureWatcher featureWatcher = mock( IKarafFeatureWatcher.class );
    IKarafBlueprintWatcher blueprintWatcher = mock( IKarafBlueprintWatcher.class );

    BundleContext bundleContext = mock( BundleContext.class );
    ServiceReference reference = mock( ServiceReference.class );
    ServiceReference reference2 = mock( ServiceReference.class );

    when( bundleContext.getServiceReference( IKarafFeatureWatcher.class ) ).thenReturn( reference );
    when( bundleContext.getService( reference )).thenReturn( featureWatcher );

    when( bundleContext.getServiceReference( IKarafBlueprintWatcher.class ) ).thenReturn( reference2 );
    when( bundleContext.getService( reference2 )).thenReturn( blueprintWatcher );

    listener.setBundleContext( bundleContext );
    t.join();
    assertEquals( 2, lifecycleManager.getPhase() );
  }

  @Test
  public void testTimeoutRelease() throws Exception {

    final BaseCountdownLatchLifecycleManager<KettleLifecycleEvent> lifecycleManager = new BaseCountdownLatchLifecycleManager<KettleLifecycleEvent>() {
      @Override protected KettleLifecycleEvent getNotificationObject() {
        return KettleLifecycleEvent.INIT;
      }
    };

    KarafLifecycleListener listener = new KarafLifecycleListener( 500 );
    lifecycleManager.addLifecycleListener( listener );
    lifecycleManager.advance();
    assertTrue( lifecycleManager.getPhase() == 1 );

    Thread t = new Thread( new Runnable() {
      @Override public void run() {
        try {
          lifecycleManager.advance();
        } catch ( InterruptedException e ) {
          fail( "error in manager" );
          e.printStackTrace();
        }
      }
    });
    t.start();

    int fatalCount = 0;
    do {
      Thread.sleep( 10 );
    } while ( t.getState() != Thread.State.WAITING && ++fatalCount < 10 );
    assertTrue( t.getState() == Thread.State.WAITING );
    assertEquals( 1, lifecycleManager.getPhase() ); // still at 1 because the first is blocked

    // At this point the KarafLifecycleListener will be blocked as we're never going to satisfy it's requirements.
    // The Timeout thread within it should release the event after the configured timeout. We'll join and wait to make
    // sure that happens
    t.join();
    assertEquals( 2, lifecycleManager.getPhase() );
  }
}