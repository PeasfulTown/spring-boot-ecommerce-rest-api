package xyz.peasfultown.ecommerce.inventory_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static org.springframework.http.ResponseEntity.status;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(Throwable.class)
    ResponseEntity<ProblemDetail> handle(HttpServletRequest req, Throwable excp) {
        if (excp instanceof ErrorResponseException)
            return status(((ErrorResponseException) excp).getStatusCode()).body(((ErrorResponseException) excp).getBody());
        // TODO: more details for internal server error?
        return status(HttpStatus.INTERNAL_SERVER_ERROR).body(ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
