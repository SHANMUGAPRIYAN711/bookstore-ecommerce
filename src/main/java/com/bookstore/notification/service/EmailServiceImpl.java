package com.bookstore.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Gmail/SMTP implementation of the email service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    /**
     * Spring abstraction used to send emails through SMTP.
     */
    private final JavaMailSender mailSender;

    /**
     * Sends a plain-text email.
     *
     * @param recipientEmail recipient email address
     * @param subject email subject
     * @param message email body
     */
    @Override
    public void sendEmail(
            String recipientEmail,
            String subject,
            String message) {

        SimpleMailMessage mailMessage =
                new SimpleMailMessage();

        mailMessage.setTo(recipientEmail);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);

        mailSender.send(mailMessage);

        log.info(
                "EMAIL SENT | recipient={} | subject={}",
                recipientEmail,
                subject
        );
    }
}