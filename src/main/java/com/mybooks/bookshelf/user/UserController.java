package com.mybooks.bookshelf.user;

import com.mybooks.bookshelf.email.token.TokenService;
import com.mybooks.bookshelf.user.payload.LoginRequest;
import com.mybooks.bookshelf.user.payload.LoginResponse;
import com.mybooks.bookshelf.user.payload.RegisterRequest;
import com.mybooks.bookshelf.user.payload.RegisterResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "UserController")
public class UserController {

    private static final String EMAIL_SENT_MESSAGE = "A new email has been sent.";
    private static final String USER_REGISTRATION_SUMMARY = "Register a new user";
    private static final String TOKEN_CONFIRMATION_SUMMARY = "Confirm user's email";
    private static final String USER_LOGIN_SUMMARY = "Login user";
    private static final String USER_ENABLED_STATUS_SUMMARY = "Check if user is enabled - if email is confirmed";
    private static final String NEW_CONFIRMATION_EMAIL_SUMMARY = "Send a new confirmation email";

    private final UserService userService;
    private final TokenService tokenService;

    public UserController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @PostMapping
    @Operation(summary = USER_REGISTRATION_SUMMARY)
    public RegisterResponse createUser(@Valid @RequestBody RegisterRequest request) {
        return userService.createUser(request);
    }

    @GetMapping("/confirmation")
    @Operation(summary = TOKEN_CONFIRMATION_SUMMARY)
    public String confirmToken(@RequestParam("token") String token) {
        return tokenService.confirmToken(token);
    }

    @PostMapping("/session")
    @Operation(summary = USER_LOGIN_SUMMARY)
    public LoginResponse loginUser(@RequestBody LoginRequest request) {
        return userService.loginUser(request);
    }

    @GetMapping("/{id}/enabled")
    @Operation(summary = USER_ENABLED_STATUS_SUMMARY)
    public boolean isEnabled(@PathVariable String id) {
        return userService.isEnabled(id);
    }

    @PostMapping("/{id}/new-confirmation-email")
    @Operation(summary = NEW_CONFIRMATION_EMAIL_SUMMARY)
    public String sendNewConfirmationEmail(@PathVariable String id) {
        userService.sendNewConfirmationEmail(id);
        return EMAIL_SENT_MESSAGE;
    }

}
