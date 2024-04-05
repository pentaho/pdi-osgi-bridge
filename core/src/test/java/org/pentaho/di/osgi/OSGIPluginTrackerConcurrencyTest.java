/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.osgi;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.pentaho.di.osgi.service.lifecycle.LifecycleEvent;
import org.pentaho.osgi.api.BeanFactory;
import org.pentaho.osgi.api.BeanFactoryLocator;
import org.pentaho.osgi.api.ProxyUnwrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by jason.dyjohnson on 1/25/16.
 */
@RunWith(MockitoJUnitRunner.class )
public class OSGIPluginTrackerConcurrencyTest {
    private OSGIPluginTracker tracker;
    private BundleContext bundleContext;
    @Mock private ProxyUnwrapper mockProxyUnwrapper;
    private Logger logger = LoggerFactory.getLogger( getClass() );

    @Before
    public void setup() throws InvalidSyntaxException {
        tracker = new OSGIPluginTracker();
        tracker.setProxyUnwrapper( mockProxyUnwrapper );
        bundleContext = mock( BundleContext.class );
        Filter filter = mock( Filter.class );
        when( mockProxyUnwrapper.unwrap( any() ) ).thenAnswer( new Answer<Object>() {
            @Override
            public Object answer( InvocationOnMock invocation ) throws Throwable {
                // return the same object that was passed in
                return invocation.getArguments()[0];
            }
        } );
    }

    @Test
    public void testGetBeanConcurrently() throws InvalidSyntaxException {
        tracker.setBundleContext(bundleContext);
        final Class<Object> clazz = Object.class;
        ServiceReference serviceReference = mock(ServiceReference.class);
        BeanFactoryLocator lookup = mock(BeanFactoryLocator.class);
        BeanFactory beanFactory = mock(BeanFactory.class);
        Bundle bundle = mock(Bundle.class);
        when(serviceReference.getBundle()).thenReturn(bundle);
        tracker.setBeanFactoryLookup(lookup);
        ExecutorService executor = Executors.newFixedThreadPool(64);
        final Object instance = new Object();
        when(bundleContext.getService(serviceReference)).thenReturn(instance);
        tracker.serviceChanged(clazz, LifecycleEvent.START, serviceReference);
        List<Future<Object>> futures = new ArrayList<Future<Object>>();
        for (int i = 1; i < 10000; ++i) {
            final String id = "TEST_ID" + Integer.toString(i);
            Object bean = new Object();
            Future future = executor.submit(new Callable() {
                @Override
                public Object call() {
                    return tracker.getBean(clazz, instance, id);
                }
            });
            futures.add(future);
        }
        executor.shutdown();
        try {
            Assert.assertTrue(executor.awaitTermination(60, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
