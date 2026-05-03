package xyz.peasfultown.ecommerce.auth_service.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import xyz.peasfultown.ecommerce.auth_service.exception.CustomErrorResponseException;
import xyz.peasfultown.ecommerce.auth_service.exception.UserServiceClientException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class UserServiceErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String s, Response response) {
        String responsebody = null;
        try {
            if (response.body() != null)
                responsebody = new String(response.body().asInputStream().readAllBytes(),
                        StandardCharsets.UTF_8);
        } catch (IOException e) {
            responsebody = "Could not read response body";
        }

        log.error("feign error method = {}, status = {}, body = {}",
            s, response.status(), responsebody);

        return new UserServiceClientException(
            HttpStatus.valueOf(response.status()),
            responsebody
            );
    }
}
