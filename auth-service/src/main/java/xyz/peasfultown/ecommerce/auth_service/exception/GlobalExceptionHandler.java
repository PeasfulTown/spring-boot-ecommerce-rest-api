package xyz.peasfultown.ecommerce.auth_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import xyz.peasfultown.ecommerce.auth_api.model.ErrorResponse;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> exceptionHandler(HttpServletRequest req,
                                                          Throwable exception) {
        HttpStatusCode status = determineHttpStatus(exception);
        ErrorResponse err = new ErrorResponse()
                .status(status.value())
                .error(exception.getClass().toString())
                .message(exception.getMessage())
                .path(req.getRequestURI())
                .timestamp(OffsetDateTime.now());

        return ResponseEntity.status(status.value()).body(err);

    }

    private HttpStatusCode determineHttpStatus(Throwable exception) {
        HttpStatusCode status;
        if (exception instanceof AccountAlreadyExistsException
                || exception instanceof InvalidAccountCredentialsException
                || exception instanceof InvalidRefreshTokenException)
            status = HttpStatus.BAD_REQUEST;
        else
            status = HttpStatus.INTERNAL_SERVER_ERROR;

        return status;
    }
}
