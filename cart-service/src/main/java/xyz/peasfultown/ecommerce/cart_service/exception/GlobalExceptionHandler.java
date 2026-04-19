package xyz.peasfultown.ecommerce.cart_service.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import xyz.peasfultown.ecommerce.cart_api.model.ErrorResponse;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private final ObjectMapper objMapper;

    @Autowired
    public GlobalExceptionHandler(ObjectMapper objMapper) {
        this.objMapper = objMapper;
    }

    @ExceptionHandler(ServiceClientException.class)
    public ResponseEntity<ErrorResponse> clientExceptionHandler(HttpServletRequest req,
                                                                ServiceClientException exception) throws Exception {
//        ErrorResponse err = objMapper.readValue(exception.getResponse().body().asInputStream(), ErrorResponse.class);
        ErrorResponse err = new ErrorResponse()
                .status(exception.getStatus().value())
                .error(exception.getClass().toString())
                .message(exception.getResponse().reason() + "@" + exception.getResponse().request().url())
                .path(req.getRequestURI())
                .timestamp(OffsetDateTime.now());

        return ResponseEntity.status(err.getStatus()).body(err);
    }

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
        if (exception instanceof ProductOutOfStockException
            || exception instanceof IllegalArgumentException
            || exception instanceof EmptyCartCheckoutException)
            status = HttpStatus.BAD_REQUEST;
        else if (exception instanceof ProductNotFoundException)
            status = HttpStatus.NOT_FOUND;
        else
            status = HttpStatus.INTERNAL_SERVER_ERROR;

        return status;
    }

}
