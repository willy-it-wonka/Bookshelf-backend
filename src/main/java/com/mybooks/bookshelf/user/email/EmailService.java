package com.mybooks.bookshelf.user.email;

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

    private static final String ENCODING = "utf-8";
    private static final String EMAIL_SUBJECT = "Confirm your email";
    private static final String SENDING_EMAIL_ERROR = "Failed to send email.";
    private static final String TEMPLATE_MESSAGE_PATH = "templates/message.html";
    private static final String NAME_PLACEHOLDER = "{name}";
    private static final String LINK_PLACEHOLDER = "{link}";

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
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, ENCODING);
            mimeMessageHelper.setText(message, true);
            mimeMessageHelper.setTo(addressee);
            mimeMessageHelper.setSubject(EMAIL_SUBJECT);
            mimeMessageHelper.setFrom(Objects.requireNonNull(fromProperties.getUsername()));
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new IllegalStateException(SENDING_EMAIL_ERROR);
        }
    }

    public String buildEmail(String name, String link) {
        String htmlContent = emailMessageLoader.loadMessage(TEMPLATE_MESSAGE_PATH);
        return htmlContent.replace(NAME_PLACEHOLDER, name).replace(LINK_PLACEHOLDER, link);
    }
}
