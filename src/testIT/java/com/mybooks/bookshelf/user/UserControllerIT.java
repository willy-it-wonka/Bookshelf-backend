package com.mybooks.bookshelf.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybooks.bookshelf.email.token.TokenService;
import com.mybooks.bookshelf.exception.EmailException;
import com.mybooks.bookshelf.exception.IncorrectPasswordException;
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
    private static final String USER_NOT_FOUND_ERROR = "User not found.";
    private static final String EMAIL_ALREADY_TAKEN_ERROR = "This email is already associated with some account.";
    private static final String INCORRECT_PASSWORD_ERROR = "Incorrect password.";

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
        RegisterRequest request = new RegisterRequest("user", "user@test.com", "123");
        RegisterResponse response = new RegisterResponse("nick", "token");
        when(userService.createUser(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andExpect(jsonPath("$.nick").value("nick"))
                .andExpect(jsonPath("$.token").value("token"));
    }

    @Test
    void whenEmailAlreadyExists_ReturnBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest("user", "user@test.com", "123");
        when(userService.createUser(any(RegisterRequest.class))).thenThrow(new EmailException(EMAIL_ALREADY_TAKEN_ERROR));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(EMAIL_ALREADY_TAKEN_ERROR));
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
    void whenLoginRequest_LoginAndReturnJwt() throws Exception {
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
    void whenLoginRequestWithIncorrectPassword_ReturnErrorMessage() throws Exception {
        LoginRequest request = new LoginRequest(EXISTING_EMAIL, WRONG_PASSWORD);
        LoginResponse response = new LoginResponse(INCORRECT_PASSWORD_ERROR, false);
        when(userService.loginUser(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value(INCORRECT_PASSWORD_ERROR));
    }

    @Test
    void whenTriesLoginNonExistentUser_ReturnErrorMessage() throws Exception {
        LoginRequest request = new LoginRequest("nonexistent@test.com", CORRECT_PASSWORD);
        LoginResponse response = new LoginResponse(USER_NOT_FOUND_ERROR, false);
        when(userService.loginUser(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value(USER_NOT_FOUND_ERROR));
    }

    @Test
    void whenCorrectUserId_ReturnUserEnabled() throws Exception {
        when(userService.isEnabled(anyString())).thenReturn(true);

        mockMvc.perform(get("/api/v1/users/{id}/enabled", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
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
        doThrow(new UsernameNotFoundException(USER_NOT_FOUND_ERROR)).when(userService).sendNewConfirmationEmail(NONEXISTENT_USER_ID);

        mockMvc.perform(post("/api/v1/users/{id}/new-confirmation-email", NONEXISTENT_USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(USER_NOT_FOUND_ERROR));
    }

    @Test
    void whenNickChangeRequest_UpdateNickAndReturnNewJwt() throws Exception {
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
        doThrow(new IncorrectPasswordException()).when(userService).changeUserNick(anyString(), any(ChangeNickRequest.class));

        mockMvc.perform(patch("/api/v1/users/{id}/nick", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(INCORRECT_PASSWORD_ERROR));
    }

    @Test
    void whenChangeNickRequestForNonExistentUserId_ReturnErrorMessage() throws Exception {
        ChangeNickRequest request = new ChangeNickRequest(NEW_NICK, CORRECT_PASSWORD);
        doThrow(new UsernameNotFoundException(USER_NOT_FOUND_ERROR)).when(userService).changeUserNick(anyString(), any(ChangeNickRequest.class));

        mockMvc.perform(patch("/api/v1/users/{id}/nick", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(USER_NOT_FOUND_ERROR));
    }

    @Test
    void whenChangeEmailRequest_ChangeEmailAndReturnSuccessMessage() throws Exception {
        String successMessage = "Your email has been successfully changed.";
        ChangeEmailRequest request = new ChangeEmailRequest(NEW_EMAIL, CORRECT_PASSWORD);
        ChangeResponse response = new ChangeResponse(successMessage);
        when(userService.changeUserEmail(eq(USER_ID), any(ChangeEmailRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/users/{id}/email", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andExpect(jsonPath("$.response").value(successMessage));
    }

    @Test
    void whenChangeEmailRequestWithIncorrectPassword_ReturnErrorMessage() throws Exception {
        ChangeEmailRequest request = new ChangeEmailRequest(NEW_EMAIL, WRONG_PASSWORD);
        when(userService.changeUserEmail(eq(USER_ID), any(ChangeEmailRequest.class))).thenThrow(new IncorrectPasswordException());

        mockMvc.perform(patch("/api/v1/users/{id}/email", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(INCORRECT_PASSWORD_ERROR));
    }

    @Test
    void whenChangeEmailRequestWithEmailAlreadyTaken_ReturnErrorMessage() throws Exception {
        ChangeEmailRequest request = new ChangeEmailRequest(EXISTING_EMAIL, CORRECT_PASSWORD);
        when(userService.changeUserEmail(eq(USER_ID), any(ChangeEmailRequest.class)))
                .thenThrow(new EmailException(EMAIL_ALREADY_TAKEN_ERROR));

        mockMvc.perform(patch("/api/v1/users/{id}/email", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(EMAIL_ALREADY_TAKEN_ERROR));
    }

    @Test
    void whenChangeEmailRequestForNonexistentUserId_ReturnErrorMessage() throws Exception {
        ChangeEmailRequest request = new ChangeEmailRequest(NEW_EMAIL, CORRECT_PASSWORD);
        when(userService.changeUserEmail(eq(NONEXISTENT_USER_ID), any(ChangeEmailRequest.class)))
                .thenThrow(new UsernameNotFoundException(USER_NOT_FOUND_ERROR));

        mockMvc.perform(patch("/api/v1/users/{id}/email", NONEXISTENT_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(USER_NOT_FOUND_ERROR));
    }

    @Test
    void whenChangePasswordRequest_ChangePasswordAndReturnSuccessMessage() throws Exception {
        String successMessage = "Your password has been successfully changed.";
        ChangePasswordRequest request = new ChangePasswordRequest("newPassword", CORRECT_PASSWORD);
        ChangeResponse response = new ChangeResponse(successMessage);
        when(userService.changeUserPassword(eq(USER_ID), any(ChangePasswordRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/users/{id}/password", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andExpect(jsonPath("$.response").value(successMessage));
    }

    @Test
    void whenChangePasswordRequestWithIncorrectCurrentPassword_ReturnErrorMessage() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("newPassword", WRONG_PASSWORD);
        doThrow(new IncorrectPasswordException()).when(userService).changeUserPassword(anyString(), any(ChangePasswordRequest.class));

        mockMvc.perform(patch("/api/v1/users/{id}/password", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(INCORRECT_PASSWORD_ERROR));
    }

    @Test
    void whenChangePasswordRequestForNonexistentUserId_ReturnErrorMessage() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("newPassword", CORRECT_PASSWORD);
        doThrow(new UsernameNotFoundException(USER_NOT_FOUND_ERROR)).when(userService).changeUserPassword(eq(NONEXISTENT_USER_ID), any(ChangePasswordRequest.class));

        mockMvc.perform(patch("/api/v1/users/{id}/password", NONEXISTENT_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(USER_NOT_FOUND_ERROR));
    }

}
