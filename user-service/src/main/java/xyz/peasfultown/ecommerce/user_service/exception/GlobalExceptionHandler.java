package xyz.peasfultown.ecommerce.user_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import xyz.peasfultown.ecommerce.user_api.model.ErrorResponse;

import java.time.OffsetDateTime;

import static org.springframework.http.ResponseEntity.status;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> handle(HttpServletRequest req, Throwable exception) {
        ErrorResponse errResponse = new ErrorResponse();
        HttpStatus status = determineHttpStatus(exception);
        errResponse.status(status.value())
                .error(status.name())
                .message(exception.getMessage())
                .path(req.getRequestURI())
                .timestamp(OffsetDateTime.now());

        log.error(exception.getMessage(), exception);

        return status(status).body(errResponse);

    }

    private HttpStatus determineHttpStatus(Throwable exception) {
        HttpStatus code;

        if (exception instanceof UserAlreadyExistsException)
            code = HttpStatus.BAD_REQUEST;
        else if (exception instanceof UserNotFoundException)
            code = HttpStatus.NOT_FOUND;
        else if (exception instanceof ForbiddenException)
            code = HttpStatus.FORBIDDEN;
        else
            code = HttpStatus.INTERNAL_SERVER_ERROR;

        return code;
    }
}
