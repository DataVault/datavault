package org.datavaultplatform.broker.initialise;

import java.security.Security;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 *  By default this class is enabled in the Spring XML config, to disable it just comment it out.
 */

@Component
public class InitialiseEncryption {

    private static final Logger logger = LoggerFactory.getLogger(InitialiseEncryption.class);

    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {

        logger.info("Add Bouncy Castle Provider");

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }
}
