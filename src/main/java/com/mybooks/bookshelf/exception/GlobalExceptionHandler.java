package com.mybooks.bookshelf.exception;

import jakarta.mail.AuthenticationFailedException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.angus.mail.smtp.SMTPSendFailedException;
import org.springframework.mail.MailSendException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Optional;

import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    public static final String UNKNOWN_VALIDATION_ERROR = "Unknown email validation error.";

    @ExceptionHandler(EmailException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public String handleEmailException(EmailException e) {
        log.error(e.getMessage(), e);
        return e.getMessage();
    }

    @ExceptionHandler(TokenException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public String handleTokenException(TokenException e) {
        log.error(e.getMessage(), e);
        return e.getMessage();
    }

    @ExceptionHandler(BookNotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    @ResponseBody
    public String handleBookNotFoundException(BookNotFoundException e) {
        log.error(e.getMessage(), e);
        return e.getMessage();
    }

    @ExceptionHandler(NoteNotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    @ResponseBody
    public String handleNoteNotFoundException(NoteNotFoundException e) {
        log.error(e.getMessage(), e);
        return e.getMessage();
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public String handleUsernameNotFoundException(UsernameNotFoundException e) {
        log.error(e.getMessage(), e);
        return e.getMessage();
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    @ResponseStatus(FORBIDDEN)
    @ResponseBody
    public String handleUnauthorizedAccessException(UnauthorizedAccessException e) {
        log.error(e.getMessage(), e);
        return e.getMessage();
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public String handleIllegalStateException(IllegalStateException e) {
        log.error(e.getMessage(), e);
        return e.getMessage();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public String handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error(e.getMessage(), e);
        return e.getMessage();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public String handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error(e.getMessage(), e);
        return Optional.ofNullable(e.getBindingResult().getFieldError())
                .map(FieldError::getDefaultMessage)
                .orElse(UNKNOWN_VALIDATION_ERROR);
    }

    @ExceptionHandler(MailSendException.class)
    @ResponseStatus(SERVICE_UNAVAILABLE)
    @ResponseBody
    public String handleMailSendException(MailSendException e) {
        log.error(e.getMessage(), e);
        return e.getMessage();
    }

    @ExceptionHandler(SMTPSendFailedException.class)
    @ResponseStatus(SERVICE_UNAVAILABLE)
    @ResponseBody
    public String handleSMTPSendFailedException(SMTPSendFailedException e) {
        log.error(e.getMessage(), e);
        return e.getMessage();
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    @ResponseStatus(UNAUTHORIZED)
    @ResponseBody
    public String handleAuthenticationFailedException(AuthenticationFailedException e) {
        log.error(e.getMessage(), e);
        return e.getMessage();
    }

}
