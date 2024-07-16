package com.mybooks.bookshelfSB.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybooks.bookshelfSB.exception.EmailIssueException;
import com.mybooks.bookshelfSB.user.payload.LoginRequest;
import com.mybooks.bookshelfSB.user.payload.LoginResponse;
import com.mybooks.bookshelfSB.user.payload.RegisterRequest;
import com.mybooks.bookshelfSB.user.payload.RegisterResponse;
import com.mybooks.bookshelfSB.user.token.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // All http requests will be treated as authorized.
public class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private TokenService tokenService;

    @Test
    void whenCorrectUserDtoProvided_CreateUserAndReturnMap() throws Exception {
        RegisterRequest request = new RegisterRequest("user", "user@gmail.com", "123");
        RegisterResponse response = new RegisterResponse("nick", "token");
        when(userService.createUser(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON) // Inform Spring MVC that the content type of the request is JSON.
                        .content(objectMapper.writeValueAsString(request))) // Serialize RegisterRequest to a JSON string for the request body.
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response))) // Verify that the response content is JSON and matches the response JSON.
                .andExpect(jsonPath("$.nick").value("nick"))
                .andExpect(jsonPath("$.token").value("token"));
    }

    @Test
    void whenInvalidEmail_ReturnBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest("user", "invalid-email", "123");
        when(userService.createUser(any(RegisterRequest.class))).thenThrow(new EmailIssueException("is invalid"));

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("This email is invalid."));
    }

    @Test
    void whenEmailAlreadyExists_ReturnBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest("user", "user@gmail.com", "123");
        when(userService.createUser(any(RegisterRequest.class))).thenThrow(new EmailIssueException("is already associated with some account"));

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("This email is already associated with some account."));
    }

    @Test
    void whenTokenIsValid_ReturnConfirmationMessage() throws Exception {
        String validToken = "token";
        String response = "Token confirmed.";
        when(tokenService.confirmToken(anyString())).thenReturn(response);

        mockMvc.perform(get("/api/register/confirm")
                        .param("token", validToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(response));
    }

    @Test
    void whenTokenIsExpired_ReturnErrorMessage() throws Exception {
        String expiredToken = "expired-token";
        String response = "Token expired.";
        when(tokenService.confirmToken(anyString())).thenThrow(new IllegalStateException(response));

        mockMvc.perform(get("/api/register/confirm")
                        .param("token", expiredToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(response));
    }

    @Test
    void whenCorrectCredentialsProvided_LoginAndReturnJwt() throws Exception {
        LoginRequest request = new LoginRequest("user@gmail.com", "123");
        LoginResponse response = new LoginResponse("JWT", true);
        when(userService.loginUser(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("JWT"));
    }

    @Test
    void whenIncorrectCredentialsProvided_ReturnErrorMessage() throws Exception {
        LoginRequest request = new LoginRequest("user@gmail.com", "wrongPass");
        LoginResponse response = new LoginResponse("Incorrect password.", false);
        when(userService.loginUser(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("Incorrect password."));
    }

    @Test
    void whenTriesLoginNonExistentUser_ReturnErrorMessage() throws Exception {
        LoginRequest request = new LoginRequest("nonexistent@gmail.com", "123");
        LoginResponse response = new LoginResponse("User not found.", false);
        when(userService.loginUser(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("User not found."));
    }

    @Test
    void whenCorrectUserId_ReturnUserEnabled() throws Exception {
        String userId = "1";
        boolean isEnabled = true;
        when(userService.isEnabled(anyString())).thenReturn(isEnabled);

        mockMvc.perform(get("/api/enabled/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(isEnabled)));
    }

    @Test
    void whenCorrectUserId_SendNewConfirmationEmail() throws Exception {
        String userId = "1";

        mockMvc.perform(post("/api/new-conf-email/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("A new email has been sent."));
    }

}
