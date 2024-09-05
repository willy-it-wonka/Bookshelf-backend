package com.mybooks.bookshelf.email;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class EmailMessageLoader {

    private static final String LOADING_TEMPLATE_ERROR = "Failed to load email message template.";

    public String loadMessage(String location) {
        try {
            // ClassPathResource used to load files from resources.
            Resource resource = new ClassPathResource(location);
            // First get content of file, then convert to String using IOUtils from Apache Commons IO.
            return IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(LOADING_TEMPLATE_ERROR);
        }
    }

}
