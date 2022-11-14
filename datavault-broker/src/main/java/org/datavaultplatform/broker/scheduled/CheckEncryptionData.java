package org.datavaultplatform.broker.scheduled;

import org.datavaultplatform.broker.services.DepositsService;
import org.datavaultplatform.broker.services.EmailService;
import org.datavaultplatform.common.email.EmailTemplate;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.deposit.ComputedEncryption;
import org.datavaultplatform.common.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.bouncycastle.util.encoders.Base64;


@Component
public class CheckEncryptionData implements ScheduledTask {

    public static final String EMAIL_GROUP_NAME = "group-name";
    public static final String EMAIL_DEPOSIT_NAME = "deposit-name";
    public static final String EMAIL_DEPOSIT_ID = "deposit-id";
    public static final String EMAIL_VAULT_NAME = "vault-name";
    public static final String EMAIL_VAULT_ID = "vault-id";
    public static final String EMAIL_VAULT_RETENTION_EXPIRY = "vault-retention-expiry";
    public static final String EMAIL_VAULT_REVIEW_DATE = "vault-review-date";
    public static final String EMAIL_USER_ID = "user-id";
    public static final String EMAIL_USER_FIRST_NAME= "user-firstname";
    public static final String EMAIL_USER_LAST_NAME = "user-lastname";
    public static final String EMAIL_SIZE_BYTES = "size-bytes";
    public static final String EMAIL_TIMESTAMP = "timestamp";
    public static final String EMAIL_HAS_PERSONAL_DATA = "hasPersonalData";
    public static final String EMAIL_CHUNK_ID = "chunk-id";
    public static final String EMAIL_CHUNK_IV = "chunk-iv";
    public static final String EMAIL_ENC_CHUNK_DIGEST = "enc-chunk-digest";

    private final DepositsService depositsService;
    private final EmailService emailService;

    @Autowired
    public CheckEncryptionData(DepositsService depositsService, EmailService emailService) {
        this.depositsService = depositsService;
        this.emailService = emailService;
    }

    private static final Logger log = LoggerFactory.getLogger(CheckEncryptionData.class);

    @Scheduled(cron = ScheduledUtils.SCHEDULE_2_ENCRYPTION_CHECK)
    public void execute() {

        Date start = new Date();
        log.info("Initiating check of encryption data at " + start);

        for (Deposit deposit : this.depositsService.completed()) {
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
                            // We agreed we would do this in a different script that we run manually
                            // Instead we want to notify the team
                            if(chunkIV != null) {
                                log.info("Updating chunk IV with: " + Base64.toBase64String(chunkIV));
                                chunk.setEncIV(chunkIV);
                            }else{
                                log.info("Didn't update chunk IV.");
                            }
                            if(chunk.getEcnArchiveDigest() == null && encChunkDigest != null) {
                                log.info("Updating encChunkDigest with: " + encChunkDigest);
                                chunk.setEcnArchiveDigest(encChunkDigest);
                            }else{
                                log.info("Didn't update encChunkDigest.");
                            }

                            depositsService.updateDeposit(deposit);

                            this.sendEmails(deposit, event, chunk.getID(), Base64.toBase64String(chunkIV), encChunkDigest);

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

    private void sendEmails(Deposit deposit, Event event, String ChunkID, String chunkIV, String encChunkDigest) {
        // Get related information for emails
        Vault vault = deposit.getVault();
        Group group = vault.getGroup();
        User depositUser = deposit.getUser();

        HashMap<String, Object> model = new HashMap<>();
        model.put(EMAIL_GROUP_NAME, group.getName());
        model.put(EMAIL_DEPOSIT_NAME, deposit.getName());
        model.put(EMAIL_DEPOSIT_ID, deposit.getID());
        model.put(EMAIL_VAULT_NAME, vault.getName());
        model.put(EMAIL_VAULT_ID, vault.getID());
//        model.put("vault-retention-expiry", vault.getRetentionPolicyExpiry());
        model.put(EMAIL_VAULT_REVIEW_DATE, vault.getReviewDate());
        model.put(EMAIL_USER_ID, depositUser.getID());
        model.put(EMAIL_USER_FIRST_NAME, depositUser.getFirstname());
        model.put(EMAIL_USER_LAST_NAME, depositUser.getLastname());
        model.put(EMAIL_SIZE_BYTES, deposit.getArchiveSize());
        model.put(EMAIL_TIMESTAMP, event.getTimestamp());
        model.put(EMAIL_HAS_PERSONAL_DATA, deposit.getHasPersonalData());
        model.put(EMAIL_CHUNK_ID, ChunkID);
        model.put(EMAIL_CHUNK_IV, chunkIV);
        model.put(EMAIL_ENC_CHUNK_DIGEST, encChunkDigest);

        // Send email to group owners
        for (User groupAdmin : group.getOwners()) {
            if (groupAdmin.getEmail() != null) {
                emailService.sendTemplateMail(groupAdmin.getEmail(),
                        "Found missing IV in Deposit",
                        EmailTemplate.GROUP_ADMIN_MISSING_IV,
                        model);
            }
        }
    }

}
