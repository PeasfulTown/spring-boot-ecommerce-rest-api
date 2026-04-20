package xyz.peasfultown.ecommerce.user_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import xyz.peasfultown.ecommerce.user_api.AddressApi;
import xyz.peasfultown.ecommerce.user_api.model.Address;
import xyz.peasfultown.ecommerce.user_api.model.NewAddressReq;
import xyz.peasfultown.ecommerce.user_api.model.UpdateAddressReq;
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
    public ResponseEntity<Address> createMyAddress(String xUserId, NewAddressReq newAddressReq) throws Exception {
        return status(HttpStatus.CREATED).body(service.createAddress(xUserId, newAddressReq));
    }

    @Override
    public ResponseEntity<Address> getMyAddressById(String userId, String addressId) throws Exception {
        return ok(service.getUserAddressById(userId, addressId));
    }

    @Override
    public ResponseEntity<Address> updateMyAddressById(String userIdHeader, String addressId, UpdateAddressReq updateAddressReq) throws Exception {
        return ok(service.updateAddressById(userIdHeader, addressId, updateAddressReq));
    }

    @Override
    public ResponseEntity<Void> deleteMyAddressById(String userId, String addressId) throws Exception {
        service.deleteAddressById(userId, addressId);
        return status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<List<Address>> getAllMyAddresses(String userId) throws Exception {
        return ok(service.getAllUserAddresses(userId));
    }

    @Override
    public ResponseEntity<Void> setMyAddressAsPrimaryById(String userId, String addressId) throws Exception {
        service.setAddressAsPrimaryById(userId, addressId);
        return status(HttpStatus.NO_CONTENT).build();
    }
}
