package xyz.peasfultown.ecommerce.user_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import xyz.peasfultown.ecommerce.user_api.AddressApi;
import xyz.peasfultown.ecommerce.user_api.model.Address;
import xyz.peasfultown.ecommerce.user_api.model.AddressCreateRequest;
import xyz.peasfultown.ecommerce.user_api.model.AddressUpdateRequest;
import xyz.peasfultown.ecommerce.user_service.exception.AccessForbiddenException;
import xyz.peasfultown.ecommerce.user_service.service.AddressService;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
public class AddressController implements AddressApi {
    private final AddressService service;

    public AddressController(AddressService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<Address> createAddress(String userIdHeader, String userIdPath, AddressCreateRequest createReq) throws Exception {
        if (!userIdHeader.equals(userIdPath))
            throw new AccessForbiddenException();
        return status(HttpStatus.CREATED).body(service.createAddress(userIdHeader, createReq));
    }

    @Override
    public ResponseEntity<List<Address>> getUserAddresses(String userIdHeader, String userIdPath, String userRole) throws Exception {
        List<Address> addresses;
        if (userRole.equals("ADMIN")
                || userIdHeader.equals(userIdPath))
            addresses = service.getAddressesByUserId(userIdPath);
        else
            throw new AccessForbiddenException();
        return ok(addresses);
    }

    @Override
    public ResponseEntity<Address> getAddress(String userIdHeader, String userRoleHeader, String addressIdPath) throws Exception {
        Address address;
        if (userRoleHeader.equals("ADMIN"))
            address = service.getAddress(addressIdPath);
        else
            address = service.getAddress(userIdHeader, addressIdPath);
        return ok(address);
    }

    @Override
    public ResponseEntity<Address> updateAddress(String userIdHeader, String userRoleHeader, String addressIdPath, AddressUpdateRequest updateReq) throws Exception {
        Address address;
        if (userRoleHeader.equals("ADMIN"))
            address = service.updateAddress(addressIdPath, updateReq);
        else
            address = service.updateAddress(userIdHeader, addressIdPath, updateReq);
        return ok(address);
    }

    @Override
    public ResponseEntity<Void> deleteAddress(String userIdHeader, String userRoleHeader, String addressIdPath) throws Exception {
        if (userRoleHeader.equals("ADMIN"))
            service.deleteAddress(addressIdPath);
        else
            service.deleteAddress(userIdHeader, addressIdPath);
        return status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<Void> setAddressAsPrimary(String userIdHeader, String addressId) throws Exception {
        service.setAddressAsPrimary(userIdHeader, addressId);
        return status(HttpStatus.NO_CONTENT).build();
    }
}
