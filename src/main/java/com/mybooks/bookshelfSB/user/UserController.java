package com.mybooks.bookshelfSB.user;

import com.mybooks.bookshelfSB.user.payload.LoginRequest;
import com.mybooks.bookshelfSB.user.payload.LoginResponse;
import com.mybooks.bookshelfSB.user.payload.RegisterRequest;
import com.mybooks.bookshelfSB.user.payload.RegisterResponse;
import com.mybooks.bookshelfSB.user.token.TokenService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private static final String EMAIL_SENT_MESSAGE = "A new email has been sent.";

    private final UserService userService;
    private final TokenService tokenService;

    public UserController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @PostMapping
    public RegisterResponse createUser(@RequestBody RegisterRequest request) {
        return userService.createUser(request);
    }

    @GetMapping("/confirmation")
    public String confirmToken(@RequestParam("token") String token) {
        return tokenService.confirmToken(token);
    }

    @PostMapping("/session")
    public LoginResponse loginUser(@RequestBody LoginRequest request) {
        return userService.loginUser(request);
    }

    @GetMapping("/{id}/enabled")
    public boolean isEnabled(@PathVariable String id) {
        return userService.isEnabled(id);
    }

    @PostMapping("/{id}/new-confirmation-email")
    public String sendNewConfirmationEmail(@PathVariable String id) {
        userService.sendNewConfirmationEmail(id);
        return EMAIL_SENT_MESSAGE;
    }

}
