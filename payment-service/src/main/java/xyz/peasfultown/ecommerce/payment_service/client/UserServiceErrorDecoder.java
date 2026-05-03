package xyz.peasfultown.ecommerce.payment_service.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import xyz.peasfultown.ecommerce.payment_service.exception.UserServiceClientInternalServiceErrorException;
import xyz.peasfultown.ecommerce.payment_service.exception.UserServiceClientException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class UserServiceErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String s, Response response) {
        String body = "";

        try {
            if (response.body() != null)
                body = new String(response.body().asInputStream().readAllBytes(),
                StandardCharsets.UTF_8);
        } catch (IOException e) {
            body = "Could not read response body";
        }

        throw new UserServiceClientException(HttpStatus.valueOf(response.status()), body);
    }
}
