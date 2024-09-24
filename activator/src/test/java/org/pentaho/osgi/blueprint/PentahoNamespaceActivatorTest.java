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

package org.pentaho.osgi.blueprint;

import org.apache.aries.blueprint.NamespaceHandler;
import org.osgi.framework.BundleContext;

import java.util.Dictionary;

import static org.mockito.Mockito.any;
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
            ( Class ) any(),
            ( NamespaceHandler ) any(),
            ( Dictionary ) any()
        );
  }

  @org.junit.Test
  public void testStop() throws Exception {
    pentahoNamespaceActivator.stop( bundleContext );
  }
}