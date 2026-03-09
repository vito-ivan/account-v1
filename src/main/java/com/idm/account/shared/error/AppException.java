package com.idm.account.shared.error;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Cross-layer application exception for business and application errors.
 * <p>
 * Clean Architecture note: resides in the domain to avoid framework coupling.
 * Infrastructure (e.g., REST) should translate it into transport-specific responses.
 */
@Schema(name = "AppException", description = "Simple error envelope.")
@Getter
@JsonIgnoreProperties({"cause", "stackTrace", "suppressed", "localizedMessage"})
public class AppException extends RuntimeException {

    public enum Code {
        VALIDATION,
        NOT_FOUND,
        CONFLICT,
        DB_ERROR,
        INTERNAL
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    private final Code code;
    @Schema(name = "message", example = "Email already registered", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("message")
    private final String message;
    private final Map<String, Object> details;

    private AppException(Code code, String message, Throwable cause, Map<String, Object> details) {
        super(message, cause);
        this.code = Objects.requireNonNull(code, "code");
        this.message = (message == null || message.isBlank()) ? code.name() : message;
        this.details = details == null ? Collections.emptyMap() : Collections.unmodifiableMap(details);
    }

    public static AppException of(Code code, String message) {
        return new AppException(code, message, null, null);
    }

    public static AppException of(Code code, String message, Throwable cause) {
        return new AppException(code, message, cause, null);
    }

    public static AppException of(Code code, String message, Map<String, Object> details) {
        return new AppException(code, message, null, details);
    }

}
