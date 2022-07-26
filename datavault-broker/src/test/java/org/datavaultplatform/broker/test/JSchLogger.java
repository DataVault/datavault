package org.datavaultplatform.broker.test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JSchLogger implements com.jcraft.jsch.Logger {

  public boolean isEnabled(int level) {
    return true;
  }

  public void log(int level, String message) {
    switch(level) {
      case DEBUG:
        if(log.isDebugEnabled()){
          log.info(message);
        }
        break;
      case INFO:
        if(log.isInfoEnabled()){
          log.info(message);
        }
        break;
      case WARN:
        if(log.isWarnEnabled()){
          log.warn(message);
        }
        break;
      case ERROR:
      case FATAL:
        if(log.isErrorEnabled()){
          log.error(message);
        }
        break;
    }
  }
}
