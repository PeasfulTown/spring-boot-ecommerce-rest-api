package xyz.peasfultown.ecommerce.cart_service.client.decoder;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import xyz.peasfultown.ecommerce.cart_service.exception.CustomErrorResponseException;
import xyz.peasfultown.ecommerce.cart_service.exception.FeignProductNotFoundException;
import xyz.peasfultown.ecommerce.cart_service.exception.ProductServiceClientInternalServerErrorException;
import xyz.peasfultown.ecommerce.cart_service.exception.ProductServiceClientNotFoundException;

public class ProductServiceErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String s, Response response) {
        switch (HttpStatus.valueOf(response.status())) {
            case NOT_FOUND:
                throw new ProductServiceClientNotFoundException(response.reason());
            default:
                throw new ProductServiceClientInternalServerErrorException(response.reason());
        }
    }
}
