package com.mybooks.bookshelfSB.user.email;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class EmailMessageLoaderIT {

    @Autowired
    private EmailMessageLoader emailMessageLoader;

    @Test
    void loadMessage_WithCorrectPath_LoadsMessage() {
        String location = "templates/message.html";
        String expectedContentStart = "<!DOCTYPE html>";

        // Check if the file exists.
        assertTrue(new ClassPathResource(location).exists());

        // Load the content.
        String content = emailMessageLoader.loadMessage(location);

        // Check if content starts with expected HTML doctype declaration.
        assertTrue(content.startsWith(expectedContentStart));
    }

    @Test
    void loadMessage_WithIncorrectPath_ThrowsIllegalStateException() {
        String invalidLocation = "templates/wrong.html";

        IllegalStateException e = assertThrows(IllegalStateException.class, () ->
                emailMessageLoader.loadMessage(invalidLocation));

        assertEquals("Failed to load email message template.", e.getMessage());
    }

}
