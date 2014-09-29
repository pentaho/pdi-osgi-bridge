/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.karaf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.karaf.main.Main;

import java.io.File;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: nbaker Date: 1/19/11
 */

public class KarafHost {
  private static final KarafHost instance = new KarafHost();
  public static final String EMBEDDED_KARAF_MODE = "embedded.karaf.mode";
  private final AtomicBoolean initialized = new AtomicBoolean( false );
  private Log logger = LogFactory.getLog( getClass().getName() );

  private KarafHost() {
  }

  public static KarafHost getInstance() {
    return instance;
  }

  public void init() {
    // Should we start Karaf or are we embedded scenario?
    // TODO: migrate to unified configuration system
    if ( System.getProperty( EMBEDDED_KARAF_MODE, "false" ).equals( "true" ) ) {
      // Karaf has been started elsewhere
      return;
    }

    if ( !initialized.getAndSet( true ) ) {
      Thread karafLaunchThread = new Thread( new Runnable() {

        @Override
        public void run() {
          String root;
          try {
            root =
                new File( KarafHost.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath() )
                    .getParentFile().getAbsolutePath() + "/karaf";
          } catch ( URISyntaxException e1 ) {
            throw new RuntimeException( e1 );
          }
          System.setProperty( "karaf.home", root );
          System.setProperty( "karaf.base", root );
          System.setProperty( "karaf.data", root + "/data" );
          System.setProperty( "karaf.history", root + "/data/history.txt" );
          System.setProperty( "karaf.instances", root + "/instances" );
          System.setProperty( "karaf.startLocalConsole", "false" );
          System.setProperty( "karaf.startRemoteShell", "true" );
          System.setProperty( "karaf.lock", "false" );
          Main main = new Main( new String[ 0 ] );
          try {
            main.launch();
          } catch ( Exception e ) {
            logger.error( "Unable to launch Karaf", e );
          }
        }
      } );
      karafLaunchThread.setContextClassLoader( this.getClass().getClassLoader() );
      karafLaunchThread.start();
    }
  }
}
