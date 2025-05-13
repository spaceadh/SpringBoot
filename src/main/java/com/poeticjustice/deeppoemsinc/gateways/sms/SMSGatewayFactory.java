package com.poeticjustice.deeppoemsinc.gateways.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SMSGatewayFactory {
    private Logger logger = LoggerFactory.getLogger(SMSGatewayFactory.class);
    private final AfricasTalkingGateway africasTalkingGateway;

    public SMSGatewayFactory(AfricasTalkingGateway africasTalkingGateway) {
        this.africasTalkingGateway = africasTalkingGateway;
        
    }

    public ISMSGateway getSMSGateway(String countryCode) {
        logger.info("SMSGatewayFactory: getSMSGateway called with country code: {}", countryCode);
        return africasTalkingGateway;
    }
}