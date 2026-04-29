package xyz.peasfultown.ecommerce.auth_service.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import xyz.peasfultown.ecommerce.auth_service.exception.CustomErrorResponseException;

public class UserServiceErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String s, Response response) {
        if (response.status() == HttpStatus.NOT_FOUND.value())
            return new CustomErrorResponseException(
                HttpStatus.NOT_FOUND,
                response.reason());
        return new CustomErrorResponseException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "User service internal server error");
    }
}
