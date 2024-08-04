package com.mybooks.bookshelf;

import org.testcontainers.containers.MySQLContainer;

public class SingletonDatabaseContainer {

    private static final MySQLContainer<?> MYSQL_CONTAINER;

    static {
        MYSQL_CONTAINER = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("testDB")
                .withUsername("test")
                .withPassword("test");
        MYSQL_CONTAINER.start();
    }

    public static MySQLContainer<?> getInstance() {
        return MYSQL_CONTAINER;
    }

}
