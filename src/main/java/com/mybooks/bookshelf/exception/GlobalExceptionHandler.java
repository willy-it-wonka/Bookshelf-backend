package com.mybooks.bookshelf.exception;

import jakarta.mail.AuthenticationFailedException;
import org.eclipse.angus.mail.smtp.SMTPSendFailedException;
import org.springframework.mail.MailSendException;
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
public class GlobalExceptionHandler {

    private static final String UNKNOWN_VALIDATION_ERROR = "Unknown email validation error.";

    @ExceptionHandler(EmailException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public String handleEmailException(EmailException e) {
        return e.getMessage();
    }

    @ExceptionHandler(TokenException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public String handleTokenException(TokenException e) {
        return e.getMessage();
    }

    @ExceptionHandler(BookNotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    @ResponseBody
    public String handleBookNotFoundException(BookNotFoundException e) {
        return e.getMessage();
    }

    @ExceptionHandler(NoteNotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    @ResponseBody
    public String handleNoteNotFoundException(NoteNotFoundException e) {
        return e.getMessage();
    }

    @ExceptionHandler(ChangeUserDetailsException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public String handleChangeUserDetailsException(ChangeUserDetailsException e) {
        return e.getMessage();
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public String handleUserNotFoundException(UserNotFoundException e) {
        return e.getMessage();
    }

    @ExceptionHandler(IncorrectPasswordException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public String handleIncorrectPasswordException(IncorrectPasswordException e) {
        return e.getMessage();
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    @ResponseStatus(FORBIDDEN)
    @ResponseBody
    public String handleUnauthorizedAccessException(UnauthorizedAccessException e) {
        return e.getMessage();
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public String handleIllegalStateException(IllegalStateException e) {
        return e.getMessage();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public String handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return e.getMessage();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public String handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return Optional.ofNullable(e.getBindingResult().getFieldError())
                .map(FieldError::getDefaultMessage)
                .orElse(UNKNOWN_VALIDATION_ERROR);
    }

    @ExceptionHandler(MailSendException.class)
    @ResponseStatus(SERVICE_UNAVAILABLE)
    @ResponseBody
    public String handleMailSendException(MailSendException e) {
        return e.getMessage();
    }

    @ExceptionHandler(SMTPSendFailedException.class)
    @ResponseStatus(SERVICE_UNAVAILABLE)
    @ResponseBody
    public String handleSMTPSendFailedException(SMTPSendFailedException e) {
        return e.getMessage();
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    @ResponseStatus(UNAUTHORIZED)
    @ResponseBody
    public String handleAuthenticationFailedException(AuthenticationFailedException e) {
        return e.getMessage();
    }

}
