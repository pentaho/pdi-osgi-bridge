package org.pentaho.di.karaf;

import org.apache.commons.lang.ObjectUtils;
import org.apache.karaf.main.Main;
import org.pentaho.di.core.annotations.LifecyclePlugin;
import org.pentaho.di.core.gui.GUIOption;
import org.pentaho.di.core.lifecycle.LifeEventHandler;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.lifecycle.LifecycleListener;
import org.pentaho.di.core.plugins.PluginClassTypeMapping;

import java.io.File;

/**
 * User: nbaker
 * Date: 1/19/11
 */

@LifecyclePlugin(id="KarafPlugin")
public class KarafHost implements LifecycleListener{
  private Main main;
  private static boolean initialized;

  public KarafHost(){

  }

  public void start(){
    try {
			String root = new File( "plugins/pluginRegistry/karaf-plugin/karaf").getAbsolutePath();
			System.setProperty("karaf.home", root);
      System.setProperty("karaf.base", root);
      System.setProperty("karaf.data", root + "/data");
      System.setProperty("karaf.history", root + "/data/history.txt");
      System.setProperty("karaf.instances", root + "/instances");
			System.setProperty("karaf.startLocalConsole", "false");
			System.setProperty("karaf.startRemoteShell", "true");
      System.setProperty("karaf.lock", "false");
			main = new Main(new String[0]);
      main.launch();
    } catch (Exception e) {
			main = null;
			e.printStackTrace();
		}
  }

  public static boolean isInitialized(){
    return initialized;
  }

  public static void setInitialized(boolean inited){
    initialized = inited;
  }

  public void onExit(LifeEventHandler lifeEventHandler) throws LifecycleException {
    
  }

  public void onStart(LifeEventHandler lifeEventHandler) throws LifecycleException {
    start();
  }
}

