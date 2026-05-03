package xyz.peasfultown.ecommerce.order_service.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import xyz.peasfultown.ecommerce.order_service.exception.CustomErrorResponseException;
import xyz.peasfultown.ecommerce.order_service.exception.UserServiceClientException;
import xyz.peasfultown.ecommerce.order_service.exception.UserServiceNotFoundException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class UserServiceErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String s, Response response) {
        String body = "";
        try {
            if (response.body() != null)
                body = new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            body = "Could not read response body";
        }

        log.error("feign error method = {}, status = {}, body = {}",
                s, response.status(), body);

        return new UserServiceClientException(
                HttpStatus.valueOf(response.status()),
                body
        );
    }
}
