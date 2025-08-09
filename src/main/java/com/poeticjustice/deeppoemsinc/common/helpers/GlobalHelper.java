package com.poeticjustice.deeppoemsinc.common.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class GlobalHelper {

    private static final Logger logger = LoggerFactory.getLogger(GlobalHelper.class);

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9.!#$%&'*+-/=?^_`{|}~]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$"
    );

    public static String validateATPhoneNumber(String phoneNumber) {
        logger.debug("Validating phone number: {}", phoneNumber);
        if (phoneNumber != null && phoneNumber.startsWith("0")) {
            String formatted = "+254" + phoneNumber.substring(1);
            logger.debug("Formatted phone number: {}", formatted);
            return formatted;
        }
        return phoneNumber;
    }

    public static boolean isEmail(String emailAddress) {
        if (emailAddress == null) {
            logger.warn("Email address is null");
            return false;
        }
        boolean isValid = EMAIL_PATTERN.matcher(emailAddress).matches();
        logger.debug("Email validation for {}: {}", emailAddress, isValid);
        return isValid;
    }

    public boolean exceedsMaxLength(String value, int maxLength) {
        if (value == null) {
            logger.warn("Input value is null for length check");
            return false;
        }
        boolean exceeds = value.length() > maxLength;
        logger.debug("Length check for value '{}': exceeds {} = {}", value, maxLength, exceeds);
        return exceeds;
    }

    public static String convertToSentenceCase(String text) {
        if (text == null || text.isEmpty()) {
            logger.warn("Text is null or empty for sentence case conversion");
            return text;
        }
        String lower = text.toLowerCase();
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : lower.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }

        String converted = result.toString();
        logger.debug("Converted '{}' to sentence case: '{}'", text, converted);
        return converted;
    }
}