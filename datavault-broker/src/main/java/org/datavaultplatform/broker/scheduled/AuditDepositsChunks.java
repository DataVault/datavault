package org.datavaultplatform.broker.scheduled;

import org.datavaultplatform.broker.services.DepositsService;
import org.datavaultplatform.broker.services.EmailService;
import org.datavaultplatform.common.model.DepositChunk;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;
import java.util.List;

public class AuditDepositsChunks {


    private DepositsService depositsService;
    private EmailService emailService;

    public void setDepositsService(DepositsService depositsService) {
        this.depositsService = depositsService;
    }
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    @Scheduled(cron = "${cron.expression}")
    public void execute() throws Exception {
        Date now = new Date();
        System.out.println("Start Audit Job "+now.toString());
        List<DepositChunk> chunks = depositsService.getChunksForAudit();
        System.out.println("auditing "+chunks.size()+"chunks");
        System.out.println("Audit Job finished");
    }
}
