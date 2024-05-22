package com.mybooks.bookshelfSB.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.enabled = TRUE WHERE u.email = ?1 AND u.enabled = FALSE")
    int updateEnabled(String email);

}
