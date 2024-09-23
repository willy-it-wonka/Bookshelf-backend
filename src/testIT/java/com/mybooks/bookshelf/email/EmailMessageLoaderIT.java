package com.mybooks.bookshelf.email;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EmailMessageLoaderIT {

    @Autowired
    private EmailMessageLoader emailMessageLoader;

    @Test
    void whenCorrectPath_LoadMessage() {
        String location = "templates/message.html";
        String expectedContentStart = "<!DOCTYPE html>";

        assertTrue(new ClassPathResource(location).exists());

        String content = emailMessageLoader.loadMessage(location);

        assertTrue(content.startsWith(expectedContentStart));
    }

    @Test
    void whenIncorrectPath_ThrowIllegalStateException() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () ->
                emailMessageLoader.loadMessage("templates/wrong.html"));
        assertEquals("Failed to load email message template.", e.getMessage());
    }

}
