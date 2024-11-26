package com.mybooks.bookshelf.email;

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
    private static final String CONFIRMATION_EMAIL_SUBJECT = "Confirm your email";
    private static final String FORGOTTEN_PASSWORD_SUBJECT = "Forgotten password";
    private static final String SENDING_EMAIL_ERROR = "An error occurred when sending an email:";
    private static final String CONFIRMATION_EMAIL_ERROR = "There was a problem while sending the confirmation email. Contact the administration. You can log in and use the application.";
    private static final String FORGOTTEN_PASSWORD_ERROR = "There was a problem while sending an email. Contact the administration.";
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

    @Async
    public void sendConfirmationEmail(String addressee, String message) {
        sendEmail(addressee, CONFIRMATION_EMAIL_SUBJECT, message, CONFIRMATION_EMAIL_ERROR);
    }

    @Async
    public void sendPasswordResetEmail(String addressee, String message) {
        sendEmail(addressee, FORGOTTEN_PASSWORD_SUBJECT, message, FORGOTTEN_PASSWORD_ERROR);
    }

    public String buildEmail(String messageTemplatePath, String name, String link) {
        String htmlContent = emailMessageLoader.loadMessage(messageTemplatePath);
        return htmlContent.replace(NAME_PLACEHOLDER, name).replace(LINK_PLACEHOLDER, link);
    }

    private void sendEmail(String addressee, String subject, String message, String errorMessage) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, ENCODING);
            mimeMessageHelper.setText(message, true);
            mimeMessageHelper.setTo(addressee);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setFrom(Objects.requireNonNull(fromProperties.getUsername()));
            javaMailSender.send(mimeMessage);
        } catch (MessagingException | MailSendException e) {
            log.error(SENDING_EMAIL_ERROR, e);
            throw new IllegalStateException(errorMessage);
        }
    }

}
