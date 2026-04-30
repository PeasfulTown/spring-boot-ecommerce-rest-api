package xyz.peasfultown.ecommerce.user_service.service;

import org.springframework.stereotype.Service;
import xyz.peasfultown.ecommerce.user_api.model.Address;
import xyz.peasfultown.ecommerce.user_api.model.AddressCreateRequest;
import xyz.peasfultown.ecommerce.user_api.model.AddressUpdateRequest;
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
    public Address createAddress(String userId, AddressCreateRequest newAddressReq) {
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
    public Address getAddress(String addressId) {
        AddressEntity ae = repo.findById(UUID.fromString(addressId))
                .orElseThrow(() -> new AddressNotFoundException(String.format(
                        "Address not found by ID: %s", addressId
                )));
        return mapper.toModel(ae);
    }

    @Override
    public Address getAddress(String userId, String addressId) {
        AddressEntity ae = repo.findAddressByUserAndId(UUID.fromString(userId), UUID.fromString(addressId))
                .orElseThrow(() -> new AddressNotFoundException(String.format(
                        "Address not found by ID: %s", addressId
                )));

        return mapper.toModel(ae);
    }

    @Override
    public Address updateAddress(String addressId, AddressUpdateRequest req) {
        AddressEntity ae = repo.findById(UUID.fromString(addressId))
                .orElseThrow(() -> new AddressNotFoundException(String.format(
                        "Address not found by ID: %s", addressId
                )));

        if (Objects.nonNull(req.getNumber()))
            ae.setNumber(req.getNumber());
        if (Objects.nonNull(req.getStreet()))
            ae.setStreet(req.getStreet());
        if (Objects.nonNull(req.getCity()))
            ae.setCity(req.getCity());
        if (Objects.nonNull(req.getState()))
            ae.setState(req.getState());
        if (Objects.nonNull(req.getCountry()))
            ae.setCountry(req.getCountry());
        if (Objects.nonNull(req.getPostalCode()))
            ae.setPostalCode(req.getPostalCode());

        return mapper.toModel(ae);
    }

    @Override
    public Address updateAddress(String userId, String addressId, AddressUpdateRequest req) {
        AddressEntity ae = repo.findAddressByUserAndId(UUID.fromString(userId), UUID.fromString(addressId))
                .orElseThrow(() -> new AddressNotFoundException(String.format(
                        "Address not found by ID: %s", addressId
                )));

        if (Objects.nonNull(req.getNumber()))
            ae.setNumber(req.getNumber());
        if (Objects.nonNull(req.getStreet()))
            ae.setStreet(req.getStreet());
        if (Objects.nonNull(req.getCity()))
            ae.setCity(req.getCity());
        if (Objects.nonNull(req.getState()))
            ae.setState(req.getState());
        if (Objects.nonNull(req.getCountry()))
            ae.setCountry(req.getCountry());
        if (Objects.nonNull(req.getPostalCode()))
            ae.setPostalCode(req.getPostalCode());

        return mapper.toModel(ae);
    }

    @Override
    public void deleteAddress(String addressId) {
        AddressEntity ae = repo.findById(UUID.fromString(addressId))
                .orElseThrow(() -> new AddressNotFoundException(String.format(
                        "Address not found by ID: %s", addressId
                )));

        repo.delete(ae);
    }

    @Override
    public void deleteAddress(String userId, String addressId) {
        AddressEntity ae = repo.findAddressByUserAndId(UUID.fromString(userId), UUID.fromString(addressId))
                .orElseThrow(() -> new AddressNotFoundException(String.format(
                        "Address not found by ID: %s", addressId
                )));

        repo.delete(ae);
    }

    @Override
    public List<Address> getAddressesByUserId(String userId) {
        List<AddressEntity> aes = repo.findAddressesByUserId(UUID.fromString(userId));

        return mapper.toModel(aes);
    }

    @Override
    public void setAddressAsPrimary(String userId, String addressId) {
        List<AddressEntity> aes = repo.findAddressesByUserId(UUID.fromString(userId));
        aes.stream().forEach(a -> {
            if (a.getId().toString().equals(addressId)) a.setPrimary(true);
            else a.setPrimary(false);
        });

        repo.saveAll(aes);
    }
}
