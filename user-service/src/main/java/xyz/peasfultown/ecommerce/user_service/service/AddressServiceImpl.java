package xyz.peasfultown.ecommerce.user_service.service;

import org.springframework.stereotype.Service;
import xyz.peasfultown.ecommerce.user_api.model.Address;
import xyz.peasfultown.ecommerce.user_api.model.NewAddressReq;
import xyz.peasfultown.ecommerce.user_api.model.UpdateAddressReq;
import xyz.peasfultown.ecommerce.user_service.entity.AddressEntity;
import xyz.peasfultown.ecommerce.user_service.entity.UserEntity;
import xyz.peasfultown.ecommerce.user_service.exception.AddressNotFoundException;
import xyz.peasfultown.ecommerce.user_service.exception.UserNotFoundException;
import xyz.peasfultown.ecommerce.user_service.mapper.AddressMapper;
import xyz.peasfultown.ecommerce.user_service.repository.AddressRepository;
import xyz.peasfultown.ecommerce.user_service.repository.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class AddressServiceImpl implements AddressService {
    private final AddressRepository repo;
    private final UserRepository userRepo;
    private final AddressMapper mapper;

    public AddressServiceImpl(AddressRepository repo, UserRepository userRepo, AddressMapper mapper) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.mapper = mapper;
    }

    @Override
    public Address createAddress(String userId, NewAddressReq newAddressReq) {
        UserEntity ue = userRepo.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UserNotFoundException(String.format(
                        "User not found by ID: %s", userId
                )));

        AddressEntity ae = AddressEntity.builder()
                .number(newAddressReq.getNumber())
                .street(newAddressReq.getStreet())
                .city(newAddressReq.getCity())
                .state(newAddressReq.getState())
                .country(newAddressReq.getCountry())
                .postalCode(newAddressReq.getPostalCode())
                .isPrimary(false)
                .user(ue)
                .build();

        ae = repo.save(ae);

        return mapper.toModel(ae);
    }

    @Override
    public Address getUserAddressById(String userId, String addressId) {
        AddressEntity ae = repo.findAddressByUserAndId(UUID.fromString(userId), UUID.fromString(addressId))
                .orElseThrow(() -> new AddressNotFoundException(String.format(
                        "Address not found by ID: %s", addressId
                )));

        return mapper.toModel(ae);
    }

    @Override
    public Address updateAddressById(String userId, String addressId, UpdateAddressReq updateAddressReq) {
        AddressEntity ae = repo.findAddressByUserAndId(UUID.fromString(userId), UUID.fromString(addressId))
                .orElseThrow(() -> new AddressNotFoundException(String.format(
                        "Address not found by ID: %s", addressId
                )));

        if (Objects.nonNull(updateAddressReq.getNumber()))
            ae.setNumber(updateAddressReq.getNumber());
        if (Objects.nonNull(updateAddressReq.getStreet()))
            ae.setStreet(updateAddressReq.getStreet());
        if (Objects.nonNull(updateAddressReq.getCity()))
            ae.setCity(updateAddressReq.getCity());
        if (Objects.nonNull(updateAddressReq.getState()))
            ae.setState(updateAddressReq.getState());
        if (Objects.nonNull(updateAddressReq.getCountry()))
            ae.setCountry(updateAddressReq.getCountry());
        if (Objects.nonNull(updateAddressReq.getPostalCode()))
            ae.setPostalCode(updateAddressReq.getPostalCode());
        if (Objects.nonNull(updateAddressReq.getIsPrimary()))
            ae.setPrimary(updateAddressReq.getIsPrimary());

        return mapper.toModel(ae);
    }

    @Override
    public void deleteAddressById(String userId, String addressId) {
        AddressEntity ae = repo.findAddressByUserAndId(UUID.fromString(userId), UUID.fromString(addressId))
                .orElseThrow(() -> new AddressNotFoundException(String.format(
                        "Address not found by ID: %s", addressId
                )));

        repo.delete(ae);
    }

    @Override
    public List<Address> getAllUserAddresses(String userId) {
        List<AddressEntity> aes = repo.findAddressesByUserId(UUID.fromString(userId));

        return mapper.toModel(aes);
    }

    @Override
    public void setAddressAsPrimaryById(String userId, String addressId) {
        List<AddressEntity> aes = repo.findAddressesByUserId(UUID.fromString(userId));
        aes.stream().forEach(a -> {
            if (a.getId().toString().equals(addressId)) a.setPrimary(true);
            else a.setPrimary(false);
        });

        repo.saveAll(aes);
    }
}
