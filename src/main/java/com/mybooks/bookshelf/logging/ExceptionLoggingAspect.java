package com.mybooks.bookshelf.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ExceptionLoggingAspect {

    @AfterThrowing(pointcut = "execution(* com.mybooks.bookshelf..*Controller.*(..))", throwing = "e")
    public void logException(Throwable e) {
        log.error(e.getMessage(), e);
    }

}
