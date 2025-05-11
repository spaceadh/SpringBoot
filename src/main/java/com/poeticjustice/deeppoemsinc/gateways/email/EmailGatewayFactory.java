package com.poeticjustice.deeppoemsinc.gateways.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailGatewayFactory {

    private static final Logger logger = LoggerFactory.getLogger(EmailGatewayFactory.class);

    private final PostmarkGateway postmarkGateway;

    public EmailGatewayFactory(PostmarkGateway postmarkGateway) {
        this.postmarkGateway = postmarkGateway;
    }

    public IEmailGateway getEmailGateway(String countryCode) {
        logger.info("Postmark Email Gateway selected for country code: {}", countryCode);
        return postmarkGateway;
        // TODO: Add logic for other providers (e.g., SendGrid for specific regions)
        // switch (countryCode.toUpperCase()) {
        //     case "US":
        //         return sendGridGateway;
        //     default:
        //         return postmarkGateway;
        // }
    }
}