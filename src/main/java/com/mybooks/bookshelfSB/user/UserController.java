package com.mybooks.bookshelfSB.user;

import com.mybooks.bookshelfSB.user.token.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;

    @Autowired
    public UserController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    public String createUser(@RequestBody UserDto userDto) {
        return userService.createUser(userDto);
    }

    @GetMapping("/register/confirm")
    public String confirmToken(@RequestParam("token") String token) {
        return tokenService.confirmToken(token);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserDto userDto) {
        LoginResponse loginResponse = userService.login(userDto);
        return ResponseEntity.ok(loginResponse);
    }

    @GetMapping("/enabled/{email}")
    public boolean isEnabled(@PathVariable String email) {
        return userService.isEnabled(email);
    }

    @PostMapping("/newtoken/{email}")
    public ResponseEntity<String> sendNewConfirmationEmail(@PathVariable String email) {
        userService.sendNewConfirmationEmail(email);
        return ResponseEntity.ok("New email sent.");
    }

}
