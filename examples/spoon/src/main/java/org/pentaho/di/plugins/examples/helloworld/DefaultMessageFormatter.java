package org.pentaho.di.plugins.examples.helloworld;

/**
 * User: nbaker Date: 1/30/11
 */
public class DefaultMessageFormatter implements MessageFormatter {
  public String format( String message ) {
    return "defaultFormatter: " + message;
  }
}
