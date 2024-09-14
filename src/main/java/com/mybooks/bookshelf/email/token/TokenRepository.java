package com.mybooks.bookshelf.email.token;

import com.mybooks.bookshelf.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByConfirmationToken(String confirmationToken);

    Optional<Token> findTop1ByTokenOwnerOrderByCreationDateDesc(User tokenOwner);

    @Transactional
    @Modifying
    @Query("UPDATE Token t SET t.confirmationDate = :confirmationDate WHERE t.confirmationToken = :confirmationToken")
    int updateConfirmationDate(String confirmationToken, LocalDateTime confirmationDate);

}
