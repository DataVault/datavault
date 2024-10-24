package org.datavaultplatform.worker.tasks.retrieve;

import org.datavaultplatform.common.event.retrieve.*;
import org.datavaultplatform.worker.tasks.BaseOrderedEvents;

import java.util.List;
import java.util.stream.Stream;

public class RetrieveEvents extends BaseOrderedEvents {

    protected static final List<String> EVENT_ORDER = Stream.of(
            RetrieveStart.class,
            UserStoreSpaceAvailableChecked.class,
            ArchiveStoreRetrievedChunk.class,
            ArchiveStoreRetrievedAll.class,
            UploadedToUserStore.class,
            RetrieveComplete.class
    ).map(Class::getName).toList();

    public static final RetrieveEvents INSTANCE = new RetrieveEvents();

    private RetrieveEvents() {
        super(EVENT_ORDER);
    }
}
    
    
    
    

    

