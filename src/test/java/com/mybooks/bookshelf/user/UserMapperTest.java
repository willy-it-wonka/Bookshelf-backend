package com.mybooks.bookshelf.user;

import com.mybooks.bookshelf.user.payload.RegisterRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void whenValidRegisterRequest_ReturnUser() {
        RegisterRequest request = new RegisterRequest("Tom", "tom@test.com", "123");
        String encodedPassword = "123";

        User user = UserMapper.mapToEntity(request, encodedPassword);

        assertNotNull(user);
        assertEquals(request.nick(), user.getNick());
        assertEquals(request.email(), user.getEmail());
        assertEquals(encodedPassword, user.getPassword());
        assertEquals(UserRole.USER, user.getUserRole());
    }

    @Test
    void whenRegisterRequestIsNull_ThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> UserMapper.mapToEntity(null, "123"));
    }

    @Test
    void whenEncodedPasswordIsNull_MapToEntityWithNullPassword() {
        RegisterRequest request = new RegisterRequest("Tom", "tom@test.com", "123");

        User user = UserMapper.mapToEntity(request, null);

        assertNotNull(user);
        assertNull(user.getPassword());
    }

}
