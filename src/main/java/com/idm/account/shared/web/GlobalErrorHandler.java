package com.idm.account.shared.web;

import com.idm.account.shared.error.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestControllerAdvice
@Slf4j
public class GlobalErrorHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<AppException>> handleValidation(WebExchangeBindException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        Map<String, List<String>> details = Stream.concat(
                        ex.getFieldErrors().stream()
                                .filter(fieldError -> fieldError.getDefaultMessage() != null)
                                .map(fe -> Map.entry(fe.getField(), fe.getDefaultMessage())),
                        ex.getGlobalErrors().stream()
                                .filter(globalError -> globalError.getDefaultMessage() != null)
                                .map(ge -> Map.entry(ge.getObjectName(), ge.getDefaultMessage()))
                )
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        LinkedHashMap::new,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));

        var appEx = AppException.of(AppException.Code.VALIDATION, "Validation failed", (Map) details);
        return handleAppException(appEx);
    }

    @ExceptionHandler(AppException.class)
    public Mono<ResponseEntity<AppException>> handleAppException(AppException ex) {
        HttpStatus status = switch (ex.getCode()) {
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case DB_ERROR, INTERNAL -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        log.warn("AppException [{}]: {}", ex.getCode(), ex.getMessage());
        return Mono.just(ResponseEntity.status(status).body(ex));
    }
}
