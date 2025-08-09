package com.poeticjustice.deeppoemsinc.common.helpers;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TokenReplacementHelper {

    private static final Logger logger = LoggerFactory.getLogger(TokenReplacementHelper.class);
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\{([^}]+)\\}");

    public String replaceTokens(String template, Map<String, String> tokens) {
        if (template == null || template.isEmpty()) {
            logger.warn("Template is null or empty; returning empty string");
            return "";
        }
        if (tokens == null || tokens.isEmpty()) {
            logger.warn("Tokens map is null or empty; returning original template: {}", template);
            return template;
        }

        logger.debug("Replacing tokens in template: {}", template);
        StringBuilder result = new StringBuilder();
        Matcher matcher = TOKEN_PATTERN.matcher(template);
        int lastEnd = 0;

        while (matcher.find()) {
            // Append text before the token
            result.append(template, lastEnd, matcher.start());
            String token = matcher.group(1); // e.g., "recipientName"
            String replacement = tokens.getOrDefault(token, matcher.group(0)); // Keep {token} if not found
            if (!tokens.containsKey(token)) {
                logger.warn("No value found for token: {} in template; retaining original token", token);
            }
            result.append(replacement);
            lastEnd = matcher.end();
        }

        // Append remaining text after the last token
        result.append(template.substring(lastEnd));
        String finalMessage = result.toString();
        logger.debug("Tokenized result: {}", finalMessage);
        return finalMessage;
    }
}