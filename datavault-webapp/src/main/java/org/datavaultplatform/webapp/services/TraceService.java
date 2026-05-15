package org.datavaultplatform.webapp.services;

import org.datavaultplatform.common.util.TraceInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("database")
@ConditionalOnBean(RestService.class)
public class TraceService {
    
    private final RestService restService;
    private final String brokerActuatorUserName;
    private final String brokerActuatorPassword;
    
    public TraceService(RestService restService,
                        @Value("${broker.actuator.username:bactor}") String brokerActuatorUserName,
                        @Value("${broker.actuator.password:bactorpass}") String brokerActuatorPassword) {
        this.restService = restService;
        this.brokerActuatorUserName = brokerActuatorUserName;
        this.brokerActuatorPassword = brokerActuatorPassword;
    }
    
    public TraceInfo getTraceInfoFromBroker() {
        return restService.getTraceFromBroker(brokerActuatorUserName, brokerActuatorPassword);  
    }
    public TraceInfo getTraceInfoFromBrokerAndSendToWorker() {
        return restService.getTraceFromBrokerAndSendToWorker(brokerActuatorUserName, brokerActuatorPassword);
    }
}
