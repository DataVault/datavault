package org.datavaultplatform.broker.initialise;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.ToString;
import org.datavaultplatform.common.model.ArchiveStore;
import org.springframework.context.ApplicationEvent;

@Getter
@ToString
public class BrokerInitialisedEvent extends ApplicationEvent {

  private final List<ArchiveStore> added;

  public BrokerInitialisedEvent(Object source, ArchiveStore... added) {
    super(source);
    this.added = Arrays.stream(added).filter(Objects::nonNull).collect(Collectors.toList());
  }
}
