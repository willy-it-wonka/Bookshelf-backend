package com.mybooks.bookshelfSB.user.email;

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
    void send_CorrectDataProvided_SendsEmail() throws MessagingException {
        String addressee = "test@gmail.com";
        String message = "Message content.";
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(fromProperties.getUsername()).thenReturn("noreply@gmail.com");

        emailService.send(addressee, message);

        verify(mimeMessage).setRecipient(Message.RecipientType.TO, new InternetAddress(addressee));
        verify(mimeMessage).setSubject("Confirm your email", "utf-8");
        verify(mimeMessage).setContent(message, "text/html;charset=utf-8");
        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void send_EmailSendingFails_ThrowsIllegalStateException() {
        String addressee = "test@gmail.com";
        String message = "Message content.";
        when(javaMailSender.createMimeMessage()).thenThrow(new IllegalStateException());

        assertThrows(IllegalStateException.class, () ->
                emailService.send(addressee, message));
    }

    @Test
    void buildEmail_CorrectReplacement_ReturnsBuiltEmail() {
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

}
