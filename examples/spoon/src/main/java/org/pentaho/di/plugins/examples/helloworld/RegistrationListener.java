package org.pentaho.di.plugins.examples.helloworld;

import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.ui.xul.XulException;

import java.util.Map;

/**
 * User: nbaker Date: 1/16/11
 */
public class RegistrationListener {
  private HelloWorldSpoonPlugin plugin;

  public HelloWorldSpoonPlugin getPlugin() {
    return plugin;
  }

  public void setPlugin( HelloWorldSpoonPlugin plugin ) {
    this.plugin = plugin;
  }

  public void register( PluginInterface account, Map properties ) {

  }

  public void unregister( PluginInterface account, Map properties ) {
    try {
      System.out.println( "removing plugin..." );
      plugin.removeFromContainer();
    } catch ( XulException e ) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
  }
}
