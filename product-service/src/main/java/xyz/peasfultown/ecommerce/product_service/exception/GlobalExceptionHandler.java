package xyz.peasfultown.ecommerce.product_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.web.ErrorResponse;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import xyz.peasfultown.ecommerce.product_api.model.ErrorDetail;

import java.time.Instant;
import java.time.OffsetDateTime;

import static org.springframework.http.ResponseEntity.status;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleErrorResponseException(ErrorResponseException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail pd = ex.getBody();
        ErrorDetail errorDetail = ErrorDetail.builder()
                .title(pd.getTitle())
                .detail(pd.getDetail())
                .instance(pd.getInstance().getPath())
                .status(pd.getStatus())
                .timestamp(OffsetDateTime.now())
                .build();
        return status(pd.getStatus()).body(errorDetail);
    }
}
