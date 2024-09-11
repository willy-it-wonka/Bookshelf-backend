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
        String addressee = "test@gmail.com";
        String message = "Message content.";
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(fromProperties.getUsername()).thenReturn("noreply@gmail.com");

        emailService.send(addressee, message);

        verify(mimeMessage).setRecipient(Message.RecipientType.TO, new InternetAddress(addressee));
        verify(mimeMessage).setSubject("Confirm your email", "utf-8");
        verify(mimeMessage).setContent(message, "text/html;charset=utf-8");
        verify(mimeMessage).setFrom(new InternetAddress("noreply@gmail.com"));
        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void whenEmailSendingFails_ThrowIllegalStateException() {
        String addressee = "test@gmail.com";
        String message = "Message content.";
        when(javaMailSender.createMimeMessage()).thenThrow(new IllegalStateException());

        assertThrows(IllegalStateException.class, () ->
                emailService.send(addressee, message));
    }

    @Test
    void whenMessageIsNull_ThrowIllegalArgumentException() {
        String addressee = "test@gmail.com";

        assertThrows(IllegalArgumentException.class, () ->
                emailService.send(addressee, null));
    }

    @Test
    void whenCorrectPlaceholdersProvided_ReturnEmailWithReplacedValues() {
        String name = "John";
        String link = "http://test.com/confirm?token=";
        String templateContent = "Hi {name}, please visit {link} to activate your account.";
        when(emailMessageLoader.loadMessage("templates/message.html")).thenReturn(templateContent);

        String result = emailService.buildEmail(name, link);

        assertTrue(result.contains(name));
        assertTrue(result.contains(link));
        assertFalse(result.contains("{name}")); // Check lack of placeholder.
        assertFalse(result.contains("{link}"));
    }

    @Test
    void whenTemplateNotFound_ThrowIllegalStateException() {
        String name = "John";
        String link = "http://test.com/confirm?token=";
        when(emailMessageLoader.loadMessage(anyString())).thenThrow(new IllegalStateException("Template not found"));

        assertThrows(IllegalStateException.class, () ->
                emailService.buildEmail(name, link));
    }

}
