package org.pentaho.osgi.impl;

import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.ui.spoon.Spoon;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogService;

/**
 * User: nbaker
 * Date: 3/14/11
 */

public class OSGILogListener implements LogListener {

  LogChannelInterface log;

  public OSGILogListener() {
    this.log = Spoon.getInstance().getLog();
  }

  public void logged(LogEntry logEntry) {

    switch(logEntry.getLevel()){
      case LogService.LOG_DEBUG:
        log.logDebug(logEntry.getMessage(), logEntry.getException());
        break;
      case LogService.LOG_ERROR:
        log.logError(logEntry.getMessage(), logEntry.getException());
        break;
      case LogService.LOG_INFO:
        log.logMinimal(logEntry.getMessage(), logEntry.getException());
        break;
      case LogService.LOG_WARNING:
        log.logMinimal(logEntry.getMessage(), logEntry.getException());
        break;
    }

  }
}
