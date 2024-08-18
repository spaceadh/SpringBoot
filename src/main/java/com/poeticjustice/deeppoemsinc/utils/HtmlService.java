package com.poeticjustice.deeppoemsinc.utils;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class HtmlService {

    @Autowired
    private SpringTemplateEngine templateEngine;

    public String generateHtml(String templateName, Context context) {
        return templateEngine.process(templateName, context);
    }
}

