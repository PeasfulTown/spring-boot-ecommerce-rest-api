package xyz.peasfultown.ecommerce.payment_service.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import xyz.peasfultown.ecommerce.payment_service.exception.UserServiceClientInternalServiceErrorException;
import xyz.peasfultown.ecommerce.payment_service.exception.UserServiceClientNotFoundException;

public class UserServiceErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String s, Response response) {
        if (response.status() == HttpStatus.NOT_FOUND.value())
            throw new UserServiceClientNotFoundException(response.reason());

        throw new UserServiceClientInternalServiceErrorException(response.reason());
    }
}
