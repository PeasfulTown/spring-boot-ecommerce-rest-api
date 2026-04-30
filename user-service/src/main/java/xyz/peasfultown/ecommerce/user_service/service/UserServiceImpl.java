package xyz.peasfultown.ecommerce.user_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.peasfultown.ecommerce.user_api.model.*;
import xyz.peasfultown.ecommerce.user_service.entity.AddressEntity;
import xyz.peasfultown.ecommerce.user_service.entity.UserEntity;
import xyz.peasfultown.ecommerce.user_service.exception.AddressNotFoundException;
import xyz.peasfultown.ecommerce.user_service.exception.UserAlreadyExistsException;
import xyz.peasfultown.ecommerce.user_service.exception.UserNotFoundException;
import xyz.peasfultown.ecommerce.user_service.mapper.AddressMapper;
import xyz.peasfultown.ecommerce.user_service.mapper.UserMapper;
import xyz.peasfultown.ecommerce.user_service.repository.UserRepository;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository repo;
    private final UserMapper mapper;
    private final AddressMapper addressMapper;

    public UserServiceImpl(UserRepository repo, UserMapper mapper, AddressMapper addressMapper) {
        this.repo = repo;
        this.mapper = mapper;
        this.addressMapper = addressMapper;
    }

    @Override
    public User createUser(UserCreateRequest req) {
        repo.findUserByEmail(req.getEmail())
                .ifPresent(u -> {
                    throw new UserAlreadyExistsException(String.format(
                            "User already exists by email: %s", req.getEmail()
                    ));
                });

        UserEntity ue = UserEntity.builder()
                .id(UUID.fromString(req.getId()))
                .email(req.getEmail())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .phone(req.getPhone())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        ue = repo.save(ue);
        return mapper.toModel(ue);
    }

    @Override
    public User updateUser(String userId, UserUpdateRequest req) {
        UserEntity ue = repo.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UserNotFoundException(String.format(
                        "User not found by ID: %s", userId
                )));

        if (Objects.nonNull(req.getEmail()))
            ue.setEmail(req.getEmail());
        if (Objects.nonNull(req.getFirstName()))
            ue.setFirstName(req.getFirstName());
        if (Objects.nonNull(req.getLastName()))
            ue.setLastName(req.getLastName());
        if (Objects.nonNull(req.getPhone()))
            ue.setPhone(req.getPhone());

        return mapper.toModel(repo.save(ue));
    }

    @Override
    public User getUser(String userId) {
        UserEntity ue = repo.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UserNotFoundException(String.format(
                        "User not found by ID: %s", userId
                )));

        return mapper.toModel(ue);
    }

    @Override
    public void deleteUser(String userId) {
        UserEntity ue = repo.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UserNotFoundException(String.format(
                        "User not found by ID: %s", userId
                )));

        repo.delete(ue);
    }

    @Override
    public Page<User> getUsersPaged(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<UserEntity> ue = repo.findAll(pageable);
        return ue.map(mapper::toModel);
    }

    @Override
    public OrderInformation getOrderInfo(OrderInformationRequest req) {
        UserEntity ue = repo.findById(UUID.fromString(req.getUserId()))
                .orElseThrow(() -> new UserNotFoundException(String.format(
                        "User not found by ID: %s", req.getUserId()
                )));

        AddressEntity ae = ue.getAddresses().stream().filter(a ->
            a.getId().toString().equals(req.getAddressId())).findFirst()
                .orElseThrow(() -> new AddressNotFoundException(String.format(
                        "Address not found by ID: %s", req.getAddressId()
                )));

        OrderInformation oi = OrderInformation.builder()
                .userId(ue.getId().toString())
                .email(ue.getEmail())
                .phone(ue.getPhone())
                .fullName(ue.getFirstName() + " " + ue.getLastName())
                .address(addressMapper.toModel(ae))
                .build();

        return oi;
    }
}
