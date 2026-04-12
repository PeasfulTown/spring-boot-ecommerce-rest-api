package xyz.peasfultown.ecommerce.user_service.service;

import xyz.peasfultown.ecommerce.user_api.model.Address;
import xyz.peasfultown.ecommerce.user_api.model.NewAddressReq;
import xyz.peasfultown.ecommerce.user_api.model.UpdateAddressReq;

import java.util.List;
import java.util.UUID;

public interface AddressService {
    Address createAddress(String userId, NewAddressReq newAddressReq);

    Address getUserAddressById(String userId, UUID addressId);

    Address updateAddressById(String userId, UUID addressId, UpdateAddressReq updateAddressReq);

    void deleteAddressById(String userId, UUID addressId);

    List<Address> getAllUserAddresses(String userId);
}
