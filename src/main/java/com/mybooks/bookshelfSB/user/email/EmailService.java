package com.mybooks.bookshelfSB.user.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final JavaMailSenderImpl fromProperties;
    private final EmailMessageLoader emailMessageLoader;

    public EmailService(JavaMailSender javaMailSender, JavaMailSenderImpl fromProperties, EmailMessageLoader emailMessageLoader) {
        this.javaMailSender = javaMailSender;
        this.fromProperties = fromProperties;
        this.emailMessageLoader = emailMessageLoader;
    }

    // Send confirmation mail.
    @Async // will make it execute in a separate thread
    public void send(String addressee, String message) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");
            mimeMessageHelper.setText(message, true);
            mimeMessageHelper.setTo(addressee);
            mimeMessageHelper.setSubject("Confirm your email");
            mimeMessageHelper.setFrom(Objects.requireNonNull(fromProperties.getUsername()));
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send email.");
        }
    }

    public String buildEmail(String name, String link) {
        String htmlContent = emailMessageLoader.loadMessage("templates/message.html");
        return htmlContent.replace("{name}", name).replace("{link}", link);
    }
}
