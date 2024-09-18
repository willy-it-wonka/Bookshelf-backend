package com.mybooks.bookshelf.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.enabled = TRUE WHERE u.email = :email AND u.enabled = FALSE")
    int updateEnabled(String email);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.nick = :nick WHERE u.id = :id")
    int updateNick(Long id, String nick);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.email = :email WHERE u.id = :id")
    int updateEmail(Long id, String email);

}
