package xyz.peasfultown.ecommerce.cart_service.exception;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;

public class CustomServiceErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String s, Response response) {
        return new ServiceClientException(HttpStatus.valueOf(response.status()), response);
    }
}
