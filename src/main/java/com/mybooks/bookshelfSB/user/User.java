package com.mybooks.bookshelfSB.user;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mybooks.bookshelfSB.book.Book;
import com.mybooks.bookshelfSB.user.token.Token;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@EqualsAndHashCode
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nick;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    private Boolean locked = false;
    private Boolean enabled = false;

    @OneToMany(mappedBy = "tokenOwner")
    private Set<Token> tokens;

    @OneToMany(mappedBy = "bookOwner")
    @JsonManagedReference   // Solve problems with cyclic object serialization and lazy initialization.
    private Set<Book> books;

    public User() {
    }

    public User(String nick, String email, String password, UserRole userRole) {
        this.nick = nick;
        this.email = email;
        this.password = password;
        this.userRole = userRole;
    }

    @Override
    @Schema(hidden = true)
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(userRole.name());
        return Collections.singletonList(simpleGrantedAuthority);
    }

    // Below are the getters we need to override (impl UserDetails), for the rest: @Getter Lombok.
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
