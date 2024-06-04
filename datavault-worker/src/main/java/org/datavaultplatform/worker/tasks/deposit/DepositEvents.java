package org.datavaultplatform.worker.tasks.deposit;

import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.deposit.*;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Stream;

public abstract class DepositEvents {

    protected static final List<String> EVENT_ORDER = Stream.of(
            Start.class,

            ComputedSize.class,          //see org.datavaultplatform.worker.tasks.deposit.DepositSizeComputer.calculateTotalDepositSize

            TransferComplete.class,      //see org.datavaultplatform.worker.tasks.deposit.DepositUserStoreDownloader.transferFromUserStoreToWorker

            PackageComplete.class,       //see org.datavaultplatform.worker.tasks.deposit.DepositPackager.doPackageStep
            ComputedDigest.class,        //see org.datavaultplatform.worker.tasks.deposit.DepositPackager.doPackageStep
            ComputedChunks.class,        //CONDITIONAL see org.datavaultplatform.worker.tasks.deposit.DepositPackager.createChunks (not if encryption enabled)
            ComputedEncryption.class,    //CONDITIONAL see org.datavaultplatform.worker.tasks.deposit.DepositPackager.encryptChunks, org.datavaultplatform.worker.tasks.deposit.DepositPackager.encryptFullTar

            StartCopyUpload.class,        
            CompleteCopyUpload.class,    
            UploadComplete.class,        
            StartChunkValidation.class,   
            StartTarValidation.class,     
            CompleteTarValidation.class,   
            ValidationComplete.class,
            Complete.class      
    ).map(Class::getName).toList();
    

    
    public static boolean isLastEventBefore(Event lastEvent, Class<? extends Event> eventClass){
        if (lastEvent == null) {
            return true;
        }

        Assert.notNull(eventClass, "eventClass must not be null");
        String eventClassName = eventClass.getName();
        int eventIndex = EVENT_ORDER.indexOf(eventClassName);
        Assert.isTrue(eventIndex >= 0, "Unexpected eventClass [%s]".formatted(eventClassName));

        String lastEventClassName = lastEvent.getClass().getName();
        int lastEventClassIndex = EVENT_ORDER.indexOf(lastEventClassName);

        if (lastEventClassIndex < 0) {
            return true;
        } else {
            return lastEventClassIndex < eventIndex;
        }
    }
    
    /*
     * Used in testing
     */
    public static List<String> getEventsBefore(Class<? extends Event> event) {
        String eventClassName = event.getName();
        Assert.isTrue(EVENT_ORDER.contains(eventClassName), "The event [%s] is invalid".formatted(eventClassName));
        int eventIdx = EVENT_ORDER.indexOf(eventClassName);
        return EVENT_ORDER.subList(0, eventIdx);
    }
}
