package org.pentaho.osgi.impl;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * User: nbaker
 * Date: 11/2/11
 */
public class PluginDirWatcher extends Thread {

  private File pluginDir;
  private ConfigurationAdmin configAdmin;
  private Map<String, Configuration> configMap = new HashMap<String, Configuration>();

  public PluginDirWatcher(ConfigurationAdmin configAdmin){
    this.configAdmin = configAdmin;
    setDaemon(true);
    pluginDir = new File("osgi-plugins");
    if(!pluginDir.exists()){
      pluginDir.mkdir();
    }
  }
  @Override
  public void run() {
    while(!interrupted()){
      for(File file : pluginDir.listFiles()){
        if(file.isDirectory()){

          File[] bundleDir = file.listFiles(new FilenameFilter(){
            @Override
            public boolean accept(File f, String fileName) {
              return fileName.equals("requiredBundles");
            }
          });
          if(bundleDir.length == 0){
            continue;
          }
          try {
            Configuration config = configMap.get(file.getAbsolutePath());
            if(config == null){
              config = configAdmin.createFactoryConfiguration("org.apache.felix.fileinstall", null);
              if (config.getBundleLocation() != null)
              {
                  config.setBundleLocation(null);
              }
              configMap.put(file.getAbsolutePath(), config);
              Dictionary<String, String> dict = new Hashtable<String, String>();
              dict.put("felix.fileinstall.dir", file.getAbsolutePath());
              dict.put("PluginDirWatcher-fileName", file.getAbsolutePath());
              config.update(dict);

              config = configAdmin.createFactoryConfiguration("org.apache.felix.fileinstall", null);
              if (config.getBundleLocation() != null)
              {
                  config.setBundleLocation(null);
              }
              dict = new Hashtable<String, String>();
              dict.put("felix.fileinstall.dir", bundleDir[0].getAbsolutePath());
              config.update(dict);
            }
          } catch (IOException e) {
            e.printStackTrace();
          } 
        }
      }
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
