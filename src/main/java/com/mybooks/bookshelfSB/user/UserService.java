package com.mybooks.bookshelfSB.user;

import com.mybooks.bookshelfSB.exception.EmailIssueException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String createUser(UserDto userDto) {
        // Check if the email address is correct.
        if(!isEmailValid(userDto.getEmail()))
            throw new EmailIssueException("is invalid");

        User user = new User(
                userDto.getNick(),
                userDto.getEmail(),
                this.passwordEncoder.encode(userDto.getPassword()),
                UserRole.USER);

        // Check if the email address is taken.
        if(userExists(user))
            throw new EmailIssueException("is already associated with some account");

        userRepository.save(user);

        return user.getNick();
    }

    // Returns true if the email address is already taken.
    public boolean userExists(User user) {
        return userRepository.findByEmail(user.getEmail()).isPresent();
    }

    // It checks that the email is correct before registration.
    public static boolean isEmailValid(String email) {
        String regex = "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}";
        return email.matches(regex);
    }

}
