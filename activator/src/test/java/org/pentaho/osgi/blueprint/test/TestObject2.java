package org.pentaho.osgi.blueprint.test;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;

/**
 * Created by nbaker on 3/25/15.
 */@ExtensionPoint(
    id = "test",
    extensionPointId = "CarteStartup",
    description = "Right after the Carte webserver has started and is fully functional"
)
public class TestObject2 implements ExtensionPointInterface{
  @Override public void callExtensionPoint( LogChannelInterface logChannelInterface, Object o ) throws KettleException {

  }
}
