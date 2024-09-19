package com.mybooks.bookshelf.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybooks.bookshelf.email.token.TokenService;
import com.mybooks.bookshelf.exception.ChangeUserDetailsException;
import com.mybooks.bookshelf.exception.EmailException;
import com.mybooks.bookshelf.exception.TokenException;
import com.mybooks.bookshelf.user.payload.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // All http requests will be treated as authorized.
class UserControllerIT {

    private static final String USER_ID = "1";
    private static final String NONEXISTENT_USER_ID = "999";
    private static final String CORRECT_PASSWORD = "correctPassword";
    private static final String WRONG_PASSWORD = "wrongPassword";
    private static final String EXISTING_EMAIL = "existing@test.com";
    private static final String NEW_NICK = "newNick";
    private static final String NEW_EMAIL = "newEmail@test.com";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserServiceImpl userService;
    @MockBean
    private TokenService tokenService;

    @Test
    void whenCorrectRegisterRequest_CreateUserAndRegisterResponse() throws Exception {
        RegisterRequest request = new RegisterRequest("user", "user@gmail.com", "123");
        RegisterResponse response = new RegisterResponse("nick", "token");
        when(userService.createUser(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON) // Inform Spring MVC that the content type of the request is JSON.
                        .content(objectMapper.writeValueAsString(request))) // Serialize RegisterRequest to a JSON string for the request body.
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response))) // Verify that the response content is JSON and matches the response JSON.
                .andExpect(jsonPath("$.nick").value("nick"))
                .andExpect(jsonPath("$.token").value("token"));
    }

    @Test
    void whenEmailAlreadyExists_ReturnBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest("user", "user@gmail.com", "123");
        when(userService.createUser(any(RegisterRequest.class))).thenThrow(new EmailException("is already associated with some account"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("This email is already associated with some account."));
    }

    @Test
    void whenEmailIsInvalid_ReturnBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest("user", "invalid-email", "123");
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid email format."));
    }

    @Test
    void whenTokenIsValid_ReturnConfirmationMessage() throws Exception {
        String validToken = "token";
        String response = "Token confirmed.";
        when(tokenService.confirmToken(anyString())).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/confirmation")
                        .param("token", validToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(response));
    }

    @Test
    void whenTokenIsExpired_ReturnErrorMessage() throws Exception {
        String expiredToken = "expired-token";
        String response = "Email confirmation error: token expired.";
        when(tokenService.confirmToken(anyString())).thenThrow(new TokenException("token expired."));

        mockMvc.perform(get("/api/v1/users/confirmation")
                        .param("token", expiredToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(response));
    }

    @Test
    void whenCorrectLoginRequestProvided_LoginAndReturnJwt() throws Exception {
        LoginRequest request = new LoginRequest(EXISTING_EMAIL, CORRECT_PASSWORD);
        LoginResponse response = new LoginResponse("JWT", true);
        when(userService.loginUser(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("JWT"));
    }

    @Test
    void whenIncorrectLoginRequestProvided_ReturnErrorMessage() throws Exception {
        LoginRequest request = new LoginRequest(EXISTING_EMAIL, WRONG_PASSWORD);
        LoginResponse response = new LoginResponse("Incorrect password.", false);
        when(userService.loginUser(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("Incorrect password."));
    }

    @Test
    void whenTriesLoginNonExistentUser_ReturnErrorMessage() throws Exception {
        LoginRequest request = new LoginRequest("nonexistent@gmail.com", CORRECT_PASSWORD);
        LoginResponse response = new LoginResponse("User not found.", false);
        when(userService.loginUser(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("User not found."));
    }

    @Test
    void whenCorrectUserId_ReturnUserEnabled() throws Exception {
        boolean isEnabled = true;
        when(userService.isEnabled(anyString())).thenReturn(isEnabled);

        mockMvc.perform(get("/api/v1/users/{id}/enabled", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(isEnabled)));
    }

    @Test
    void whenNonexistentUserId_ReturnUserNotEnabled() throws Exception {
        when(userService.isEnabled(NONEXISTENT_USER_ID)).thenReturn(false);

        mockMvc.perform(get("/api/v1/users/{id}/enabled", NONEXISTENT_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void whenCorrectUserId_SendNewConfirmationEmail() throws Exception {
        mockMvc.perform(post("/api/v1/users/{id}/new-confirmation-email", USER_ID))
                .andExpect(status().isOk())
                .andExpect(content().string("A new email has been sent."));
    }

    @Test
    void whenNonexistentUserIdRequestForNewConfirmationEmail_ReturnErrorMessage() throws Exception {
        String errorMessage = "User not found";
        doThrow(new UsernameNotFoundException(errorMessage)).when(userService).sendNewConfirmationEmail(NONEXISTENT_USER_ID);

        mockMvc.perform(post("/api/v1/users/{id}/new-confirmation-email", NONEXISTENT_USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));
    }

    @Test
    void whenNickChangeRequest_UpdateNickAndReturnJwt() throws Exception {
        ChangeNickRequest request = new ChangeNickRequest(NEW_NICK, CORRECT_PASSWORD);
        ChangeResponse response = new ChangeResponse("jwt");

        when(userService.changeUserNick(anyString(), any(ChangeNickRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/users/{id}/nick", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andExpect(jsonPath("$.response").value("jwt"));
    }

    @Test
    void whenNickChangeRequestWithIncorrectPassword_ReturnErrorMessage() throws Exception {
        ChangeNickRequest request = new ChangeNickRequest(NEW_NICK, WRONG_PASSWORD);
        String errorMessage = "Incorrect password.";

        doThrow(new ChangeUserDetailsException(errorMessage)).when(userService).changeUserNick(anyString(), any(ChangeNickRequest.class));

        mockMvc.perform(patch("/api/v1/users/{id}/nick", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));
    }

    @Test
    void whenChangeNickRequestForNonExistentUserId_ReturnErrorMessage() throws Exception {
        ChangeNickRequest request = new ChangeNickRequest(NEW_NICK, CORRECT_PASSWORD);
        String errorMessage = "User not found.";

        doThrow(new UsernameNotFoundException(errorMessage)).when(userService).changeUserNick(anyString(), any(ChangeNickRequest.class));

        mockMvc.perform(patch("/api/v1/users/{id}/nick", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));
    }

    @Test
    void whenChangeEmailRequest_ChangeEmailAndReturnSuccessMessage() throws Exception {
        ChangeEmailRequest request = new ChangeEmailRequest(NEW_EMAIL, CORRECT_PASSWORD);
        ChangeResponse response = new ChangeResponse("Your email has been successfully changed.");

        when(userService.changeUserEmail(eq(USER_ID), any(ChangeEmailRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/users/{id}/email", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andExpect(jsonPath("$.response").value("Your email has been successfully changed."));
    }

    @Test
    void whenChangeEmailRequestWithIncorrectPassword_ReturnErrorMessage() throws Exception {
        ChangeEmailRequest request = new ChangeEmailRequest(NEW_EMAIL, WRONG_PASSWORD);
        when(userService.changeUserEmail(eq(USER_ID), any(ChangeEmailRequest.class)))
                .thenThrow(new ChangeUserDetailsException("Incorrect password."));

        mockMvc.perform(patch("/api/v1/users/{id}/email", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Incorrect password."));
    }

    @Test
    void whenChangeEmailRequestWithEmailAlreadyTaken_ReturnErrorMessage() throws Exception {
        ChangeEmailRequest request = new ChangeEmailRequest(EXISTING_EMAIL, CORRECT_PASSWORD);
        when(userService.changeUserEmail(eq(USER_ID), any(ChangeEmailRequest.class)))
                .thenThrow(new EmailException("is already associated with some account"));

        mockMvc.perform(patch("/api/v1/users/{id}/email", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("This email is already associated with some account."));
    }

    @Test
    void whenChangeEmailRequestForNonexistentUserId_ReturnErrorMessage() throws Exception {
        ChangeEmailRequest request = new ChangeEmailRequest(NEW_EMAIL, CORRECT_PASSWORD);
        when(userService.changeUserEmail(eq(NONEXISTENT_USER_ID), any(ChangeEmailRequest.class)))
                .thenThrow(new UsernameNotFoundException("User not found."));

        mockMvc.perform(patch("/api/v1/users/{id}/email", NONEXISTENT_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not found."));
    }

}
