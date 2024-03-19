package com.mybooks.bookshelfSB.user.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServer {

    private final JavaMailSender javaMailSender;

    @Autowired
    public EmailServer(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

}
