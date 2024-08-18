package com.poeticjustice.deeppoemsinc.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private TemplateEngine templateEngine;

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Value("${spring.mail.username}")
    private String senderEmail;
    // private String senderEmail = env.getProperty("email.sender.address");

    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        logger.info("senderEmail: " + senderEmail);
        message.setFrom(senderEmail); // Use the injected sender email address
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }

    public void sendHtmlMessage(String to, String subject, String templateName, Context context) throws MessagingException, jakarta.mail.MessagingException {
        jakarta.mail.internet.MimeMessage mimeMessage = emailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);

        String htmlBody = templateEngine.process(templateName, context);

        messageHelper.setFrom(senderEmail);
        messageHelper.setTo(to);
        messageHelper.setSubject(subject);
        messageHelper.setText(htmlBody, true);

        emailSender.send(mimeMessage);
        logger.info("HTML email sent to: {}", to);
    }

    public void sendHtmlMessageWithAttachment(String to, String subject, String templateName, Context context, File attachment) throws MessagingException, jakarta.mail.MessagingException {
        jakarta.mail.internet.MimeMessage mimeMessage = emailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);

        // Generate HTML content
        String htmlBody = templateEngine.process(templateName, context);

        // Set up the email
        messageHelper.setFrom(senderEmail);
        messageHelper.setTo(to);
        messageHelper.setSubject(subject);
        messageHelper.setText(htmlBody, true);

        // Add attachment
        messageHelper.addAttachment(attachment.getName(), attachment);

        // Send the email
        emailSender.send(mimeMessage);
        logger.info("HTML email with attachment sent to: {}", to);
    }

}
