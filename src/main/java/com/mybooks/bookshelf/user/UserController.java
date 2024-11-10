package com.mybooks.bookshelf.user;

import com.mybooks.bookshelf.email.token.TokenService;
import com.mybooks.bookshelf.user.payload.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

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
    private static final String NICK_CHANGE_SUMMARY = "Change user nick";
    private static final String EMAIL_CHANGE_SUMMARY = "Change user email";
    private static final String PASSWORD_CHANGE_SUMMARY = "Change user password";

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
    public RedirectView confirmToken(@RequestParam("token") String token) {
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

    @PatchMapping("/{id}/nick")
    @Operation(summary = NICK_CHANGE_SUMMARY)
    public ChangeResponse changeUserNick(@PathVariable String id, @Valid @RequestBody ChangeNickRequest request) {
        return userService.changeUserNick(id, request);
    }

    @PatchMapping("/{id}/email")
    @Operation(summary = EMAIL_CHANGE_SUMMARY)
    public ChangeResponse changeUserEmail(@PathVariable String id, @Valid @RequestBody ChangeEmailRequest request) {
        return userService.changeUserEmail(id, request);
    }

    @PatchMapping("/{id}/password")
    @Operation(summary = PASSWORD_CHANGE_SUMMARY)
    public ChangeResponse changeUserPassword(@PathVariable String id, @Valid @RequestBody ChangePasswordRequest request) {
        return userService.changeUserPassword(id, request);
    }

}
