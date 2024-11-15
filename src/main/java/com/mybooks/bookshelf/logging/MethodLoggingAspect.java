package com.mybooks.bookshelf.logging;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class MethodLoggingAspect {

    private static final String POINTCUT =
                    "execution(* com.mybooks.bookshelf..*.*(..))" +
                    " && !within(com.mybooks.bookshelf.security.JsonWebTokenFilter)" +
                    " && !within(com.mybooks.bookshelf.OpenApiConfig)";

    private long startTime;

    @Before("within(@org.springframework.web.bind.annotation.RestController *)")
    public void logEndpoint(JoinPoint joinPoint) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String httpMethod = request.getMethod();
            String requestURL = request.getRequestURL().toString();
            log.info("ACCESSED ENDPOINT: {} {} WITH METHOD: {}.{}", httpMethod, requestURL, className, methodName);
        }
    }

    @Before(POINTCUT)
    public void logMethodStart(JoinPoint joinPoint) {
        startTime = System.currentTimeMillis();
        log.info("CALLED: {} WITH ARGUMENTS: {}", joinPoint.getSignature(), Arrays.toString(joinPoint.getArgs()));
    }

    @AfterReturning(pointcut = POINTCUT, returning = "result")
    public void logMethodReturn(JoinPoint joinPoint, Object result) {
        long duration = System.currentTimeMillis() - startTime;
        log.info("METHOD {} RETURNED: {}", joinPoint.getSignature(), result);
        log.info("EXECUTION TIME: {}ms", duration);
    }

}
