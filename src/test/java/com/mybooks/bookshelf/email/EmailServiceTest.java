package com.mybooks.bookshelf.email;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    private static final String ADDRESSEE = "tom@test.com";
    private static final String MESSAGE = "Message content";
    private static final String USER_NAME = "John";
    private static final String CONFIRMATION_ENDPOINT = "http://test.com/api/v1/users/confirmation?token=";
    private static final String CONFIRMATION_TEMPLATE_PATH = "templates/confirmation-email.html";

    private JavaMailSender javaMailSender;
    private JavaMailSenderImpl fromProperties;
    private EmailMessageLoader emailMessageLoader;
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        javaMailSender = mock(JavaMailSender.class);
        fromProperties = mock(JavaMailSenderImpl.class);
        emailMessageLoader = mock(EmailMessageLoader.class);
        emailService = new EmailService(javaMailSender, fromProperties, emailMessageLoader);
    }

    @Test
    void whenCorrectEmailDataProvided_SendEmail() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(fromProperties.getUsername()).thenReturn("noreply@gmail.com");

        emailService.sendConfirmationEmail(ADDRESSEE, MESSAGE);

        verify(mimeMessage).setRecipient(Message.RecipientType.TO, new InternetAddress(ADDRESSEE));
        verify(mimeMessage).setSubject("Confirm your email", "utf-8");
        verify(mimeMessage).setContent(MESSAGE, "text/html;charset=utf-8");
        verify(mimeMessage).setFrom(new InternetAddress("noreply@gmail.com"));
        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void whenEmailSendingFails_ThrowIllegalStateException() {
        when(javaMailSender.createMimeMessage()).thenThrow(new IllegalStateException());
        assertThrows(IllegalStateException.class, () -> emailService.sendConfirmationEmail(ADDRESSEE, MESSAGE));
    }

    @Test
    void whenMessageIsNull_ThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> emailService.sendConfirmationEmail(ADDRESSEE, null));
    }

    @Test
    void whenCorrectPlaceholdersProvided_ReturnEmailWithReplacedValues() {
        String templateContent = "Hi {name}, please visit {link} to activate your account.";
        when(emailMessageLoader.loadMessage("templates/confirmation-email.html")).thenReturn(templateContent);

        String result = emailService.buildEmail(CONFIRMATION_TEMPLATE_PATH, USER_NAME, CONFIRMATION_ENDPOINT);

        assertTrue(result.contains(USER_NAME));
        assertTrue(result.contains(CONFIRMATION_ENDPOINT));
        assertFalse(result.contains("{name}")); // Check lack of placeholder.
        assertFalse(result.contains("{link}"));
    }

    @Test
    void whenTemplateNotFound_ThrowIllegalStateException() {
        when(emailMessageLoader.loadMessage(anyString())).thenThrow(new IllegalStateException("Failed to load email message template."));
        assertThrows(IllegalStateException.class, () -> emailService.buildEmail(CONFIRMATION_TEMPLATE_PATH, USER_NAME, CONFIRMATION_ENDPOINT));
    }

}
