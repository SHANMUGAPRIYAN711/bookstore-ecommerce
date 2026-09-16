package com.bookstore.notification.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    void shouldSendEmail() {

        String recipientEmail =
                "customer@gmail.com";

        String subject =
                "Order Confirmed";

        String message =
                "Your order has been confirmed successfully.";

        emailService.sendEmail(
                recipientEmail,
                subject,
                message
        );

        ArgumentCaptor<SimpleMailMessage> mailCaptor =
                ArgumentCaptor.forClass(
                        SimpleMailMessage.class
                );

        verify(mailSender).send(
                mailCaptor.capture()
        );

        SimpleMailMessage sentMail =
                mailCaptor.getValue();

        assertEquals(
                recipientEmail,
                sentMail.getTo()[0]
        );

        assertEquals(
                subject,
                sentMail.getSubject()
        );

        assertEquals(
                message,
                sentMail.getText()
        );
    }
}