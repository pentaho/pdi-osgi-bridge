package org.pentaho.di.osgi;

import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogService;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.ui.spoon.Spoon;

/**
 * User: nbaker
 * Date: 2/16/11
 */
public class OSGILogListener implements LogListener {

  public OSGILogListener() {
  }

  public void logged(LogEntry logEntry) {
    LogChannelInterface log =Spoon.getInstance().getLog();

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
