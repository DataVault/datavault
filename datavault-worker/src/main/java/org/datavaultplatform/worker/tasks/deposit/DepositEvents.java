package org.datavaultplatform.worker.tasks.deposit;

import org.datavaultplatform.common.event.deposit.*;
import org.datavaultplatform.worker.tasks.BaseOrderedEvents;

import java.util.List;
import java.util.stream.Stream;

public class DepositEvents extends BaseOrderedEvents {

    protected static final List<String> EVENT_ORDER = Stream.of(
            Start.class,

            ComputedSize.class,

            TransferComplete.class,

            PackageComplete.class,
            ComputedDigest.class,
            ComputedChunks.class,
            ComputedEncryption.class,

            StartCopyUpload.class,
            CompleteCopyUpload.class,
            UploadComplete.class,
            StartChunkValidation.class,
            StartTarValidation.class,
            CompleteTarValidation.class,
            ValidationComplete.class,
            Complete.class
    ).map(Class::getName).toList();

    private DepositEvents() {
        super(EVENT_ORDER);
    }

    public static final DepositEvents INSTANCE = new DepositEvents();

}
