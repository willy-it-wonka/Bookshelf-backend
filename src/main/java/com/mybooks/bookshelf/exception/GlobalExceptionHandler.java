package com.mybooks.bookshelf.exception;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
public class GlobalExceptionHandler {

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
        return e.getFieldError().getDefaultMessage();
    }

}
