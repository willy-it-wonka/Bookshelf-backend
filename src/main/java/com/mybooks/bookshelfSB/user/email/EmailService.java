package com.mybooks.bookshelfSB.user.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public EmailService(JavaMailSender javaMailSender, JavaMailSenderImpl fromProperties) {
        this.javaMailSender = javaMailSender;
        this.fromProperties = fromProperties;
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

}
