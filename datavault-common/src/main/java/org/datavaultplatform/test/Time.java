package org.datavaultplatform.test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Time {

  private final LocalDateTime time;

  public Time(LocalDateTime time){
    this.time = time;
  }

  public String getTime(){
    return DateTimeFormatter.ISO_DATE_TIME.format(time);
  }
}
