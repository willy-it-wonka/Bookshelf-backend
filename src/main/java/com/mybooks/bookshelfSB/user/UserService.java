package com.mybooks.bookshelfSB.user;

import com.mybooks.bookshelfSB.exception.EmailAlreadyTakenException;
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
        User user = new User(
                userDto.getNick(),
                userDto.getEmail(),
                this.passwordEncoder.encode(userDto.getPassword()),
                UserRole.USER);

        // If the email address is already taken, throw an exception, else sign up a new user.
        if(userExists(user))
            throw new EmailAlreadyTakenException(userDto.getEmail());
        userRepository.save(user);

        return user.getNick();
    }

    // Returns true if the email address is already taken.
    public boolean userExists(User user) {
        return userRepository.findByEmail(user.getEmail()).isPresent();
    }
}
