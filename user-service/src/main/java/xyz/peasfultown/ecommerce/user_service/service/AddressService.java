package xyz.peasfultown.ecommerce.user_service.service;

import xyz.peasfultown.ecommerce.user_api.model.Address;
import xyz.peasfultown.ecommerce.user_api.model.NewAddressReq;
import xyz.peasfultown.ecommerce.user_api.model.UpdateAddressReq;

import java.util.List;
import java.util.UUID;

public interface AddressService {
    Address createAddress(String userId, NewAddressReq newAddressReq);

    Address getUserAddressById(String userId, String addressId);

    Address updateAddressById(String userId, String addressId, UpdateAddressReq updateAddressReq);

    void deleteAddressById(String userId, String addressId);

    List<Address> getAllUserAddresses(String userId);

    void setAddressAsPrimaryById(String userId, String addressId);
}
