package xyz.peasfultown.ecommerce.user_service.service;

import xyz.peasfultown.ecommerce.user_api.model.Address;
import xyz.peasfultown.ecommerce.user_api.model.AddressCreateRequest;
import xyz.peasfultown.ecommerce.user_api.model.AddressUpdateRequest;

import java.util.List;

public interface AddressService {
    Address createAddress(String userId, AddressCreateRequest createReq);

    Address getAddress(String addressId);

    Address getAddress(String userId, String addressId);

    Address updateAddress(String addressIdPath, AddressUpdateRequest updateReq);

    Address updateAddress(String userId, String addressId, AddressUpdateRequest updateAddressReq);

    void deleteAddress(String addressIdPath);

    void deleteAddress(String userId, String addressId);

    List<Address> getAddressesByUserId(String userId);

    void setAddressAsPrimary(String userId, String addressId);

}
