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
import java.util.UUID;

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
    public ResponseEntity<Address> getMyAddressById(String xUserId, UUID id) throws Exception {
        return ok(service.getUserAddressById(xUserId, id));
    }

    @Override
    public ResponseEntity<Address> updateMyAddressById(String xUserId, UUID id, UpdateAddressReq updateAddressReq) throws Exception {
        return ok(service.updateAddressById(xUserId, id, updateAddressReq));
    }

    @Override
    public ResponseEntity<Void> deleteMyAddressById(String xUserId, UUID id) throws Exception {
        service.deleteAddressById(xUserId, id);
        return status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<List<Address>> getAllMyAddresses(String xUserId) throws Exception {
        return ok(service.getAllUserAddresses(xUserId));
    }
}
