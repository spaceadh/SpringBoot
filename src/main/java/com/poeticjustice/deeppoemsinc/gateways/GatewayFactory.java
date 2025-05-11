package com.poeticjustice.deeppoemsinc.gateways;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class GatewayFactory {

    private final IServiceProvider serviceProvider;
    private final Map<String, String> configuration;
    private final Logger logger;

    public GatewayFactory(IServiceProvider serviceProvider, Map<String, String> configuration, Logger<GatewayFactory> logger) {
        this.serviceProvider = serviceProvider;
        this.configuration = configuration;
        this.logger = logger;
    }

    public ISMSGateway getSMSGateway(String countryCode) {
        switch (countryCode.toUpperCase()) {
            case "KE":
                logger.info("AfricasTalking SMS Gateway selected for country code: {}", countryCode);
                return serviceProvider.getRequiredService(AfricasTalkingGateway.class, new AfricasTalkingGateway(configuration));
            // case "US":
            // case "EU":
            //     logger.info("InfoBip SMS Gateway is currently not implemented.");
            //     return null; // Placeholder for InfoBip integration
            default:
                logger.warn("No SMS provider found for country code: {}. Defaulting to AfricasTalking.", countryCode);
                return serviceProvider.getRequiredService(AfricasTalkingGateway.class, new AfricasTalkingGateway(configuration));
        }
    }
}