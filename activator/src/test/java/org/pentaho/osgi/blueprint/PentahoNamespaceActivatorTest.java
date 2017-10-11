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

import org.apache.aries.blueprint.NamespaceHandler;
import org.osgi.framework.BundleContext;

import java.util.Dictionary;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PentahoNamespaceActivatorTest {

  BundleContext bundleContext;
  PentahoNamespaceActivator pentahoNamespaceActivator;

  @org.junit.Before
  public void setUp() throws Exception {
    bundleContext = mock( BundleContext.class );
    pentahoNamespaceActivator = new PentahoNamespaceActivator();
  }

  @org.junit.After
  public void tearDown() throws Exception {

  }

  @org.junit.Test
  public void testStart() throws Exception {
    pentahoNamespaceActivator.start( bundleContext );
    verify( bundleContext ).
        registerService(
            ( Class ) anyObject(),
            ( NamespaceHandler ) anyObject(),
            ( Dictionary ) anyObject()
        );
  }

  @org.junit.Test
  public void testStop() throws Exception {
    pentahoNamespaceActivator.stop( bundleContext );
  }
}