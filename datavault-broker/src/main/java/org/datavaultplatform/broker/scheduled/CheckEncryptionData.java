package org.datavaultplatform.broker.scheduled;

import org.datavaultplatform.broker.services.DepositsService;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.deposit.ComputedEncryption;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.DepositChunk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;
import java.util.concurrent.TimeUnit;


public class CheckEncryptionData {

    private DepositsService depositsService;

    public void setDepositsService(DepositsService depositsService) {
        this.depositsService = depositsService;
    }

    private static final Logger log = LoggerFactory.getLogger(CheckEncryptionData.class);

    @Scheduled(cron = "${cron.expression}")
    public void checkAll() throws Exception {

        Date start = new Date();
        log.info("Initiating check of encryption data at " + start);

        for (Deposit deposit : depositsService.completed()) {
            log.info("Checking encryption data for deposit " + deposit.getID() + " name - " + deposit.getName());

            // Look for any faulty chunks with no encryption data
            for (DepositChunk chunk : deposit.getDepositChunks()) {
                if ((chunk.getEncIV() == null) || (chunk.getEncIV().length == 0)) {
                    log.info("chunk " + chunk.getChunkNum() + " is empty!!!!");

                    // We found a dodgy chunk, so look for a matching Event that contains the missing data.
                    for (Event event : deposit.getEvents()) {

                        if (event instanceof ComputedEncryption) {

                            ComputedEncryption computedEncryptionEvent = (ComputedEncryption) event;

                            HashMap<Integer, byte[]> chunkIVs = computedEncryptionEvent.getChunkIVs();
                            byte[] chunkIV = chunkIVs.get(chunk.getChunkNum());
                            log.info("The encIV in the Event table is " + Arrays.toString(chunkIV));

                            HashMap<Integer, String> encChunkDigests = computedEncryptionEvent.getEncChunkDigests();
                            String encChunkDigest = encChunkDigests.get(chunk.getChunkNum());
                            log.info("The encChunkDigest in the Event table is " + encChunkDigest);

                            // Now the risky bit, update the deposit chunk with the data from the event.
                            chunk.setEncIV(chunkIV);
                            chunk.setEcnArchiveDigest(encChunkDigest);
                            depositsService.updateDeposit(deposit);

                            // Break out of the Events loop. I hate break statements, but I don't have time to code this better.
                            break;
                        }
                    }
                }
            }
        }

        Date end = new Date();
        log.info("Finished check of encryption data at " + start);
        log.info("Check took " + TimeUnit.MILLISECONDS.toSeconds(end.getTime() - start.getTime()) + " seconds");
    }
}
