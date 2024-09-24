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
package org.pentaho.di.plugins.examples.helloworld;

/**
 * User: nbaker Date: 1/30/11
 */
public class DefaultMessageFormatter implements MessageFormatter {
  public String format( String message ) {
    return "defaultFormatter: " + message;
  }
}
