package xyz.peasfultown.ecommerce.order_service.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import xyz.peasfultown.ecommerce.order_service.exception.CustomErrorResponseException;
import xyz.peasfultown.ecommerce.order_service.exception.FeignUserServiceNotFoundException;

public class UserServiceErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String s, Response response) {
        if (response.status() == HttpStatus.NOT_FOUND.value())
            throw new FeignUserServiceNotFoundException();
        else
            throw new CustomErrorResponseException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Exception when making request to user service"
            );
    }
}
