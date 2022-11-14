package org.datavaultplatform.common.model.dao;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SQLAppender extends AppenderBase<ILoggingEvent> {

  @Override
  protected void append(ILoggingEvent event) {
    //TODO - maybe be used to capture SQL - remember one JPA/Hibernate query can result in multiple SQL selects
    log.info(event.getFormattedMessage());
  }

}
