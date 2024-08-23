package com.mybooks.bookshelf.user.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class EmailService {

    private static final String ENCODING = "utf-8";
    private static final String EMAIL_SUBJECT = "Confirm your email";
    private static final String SENDING_EMAIL_ERROR = "An error occurred when sending the confirmation email:";
    private static final String ERROR_MESSAGE = "There was a problem while sending confirmation email. Contact the administration. You can log in and use the application.";
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
    @Async // Will make it execute in a separate thread.
    public void send(String addressee, String message) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, ENCODING);
            mimeMessageHelper.setText(message, true);
            mimeMessageHelper.setTo(addressee);
            mimeMessageHelper.setSubject(EMAIL_SUBJECT);
            mimeMessageHelper.setFrom(Objects.requireNonNull(fromProperties.getUsername()));
            javaMailSender.send(mimeMessage);
        } catch (MessagingException | MailSendException e) {
            log.error(SENDING_EMAIL_ERROR, e);
            throw new IllegalStateException(ERROR_MESSAGE);
        }
    }

    public String buildEmail(String name, String link) {
        String htmlContent = emailMessageLoader.loadMessage(TEMPLATE_MESSAGE_PATH);
        return htmlContent.replace(NAME_PLACEHOLDER, name).replace(LINK_PLACEHOLDER, link);
    }
}
