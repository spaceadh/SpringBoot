package com.poeticjustice.deeppoemsinc.gateways;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GatewayFactory {

    private final ApplicationContext applicationContext;
    private final Map<String, String> configuration;
    private static final String DEFAULT_GATEWAY = "AfricasTalking";
    private final Map<String, String> countryToGatewayMap;
    private Logger logger = LoggerFactory.getLogger(GatewayFactory.class);

    public GatewayFactory(ApplicationContext applicationContext, Map<String, String> configuration) {
        this.applicationContext = applicationContext;
        this.configuration = configuration;
        this.countryToGatewayMap = Map.of(
            "KE", "AfricasTalking",
            "US", "Twilio",  // Example
            "EU", "Infobip"  // Example
        );
        this.logger = logger;
    }

    public ISMSGateway getSMSGateway(String countryCode) {
        String gatewayName = countryToGatewayMap.getOrDefault(
            countryCode.toUpperCase(), 
            DEFAULT_GATEWAY
        );
        
        try {
            ISMSGateway gateway = (ISMSGateway) applicationContext.getBean(
                gatewayName + "Gateway", 
                ISMSGateway.class
            );
            logger.info("Selected {} SMS Gateway for country code: {}", gatewayName, countryCode);
            return gateway;
        } catch (Exception e) {
            logger.error("Failed to initialize gateway {}: {}", gatewayName, e.getMessage());
            return applicationContext.getBean(DEFAULT_GATEWAY + "Gateway", ISMSGateway.class);
        }
    }
}