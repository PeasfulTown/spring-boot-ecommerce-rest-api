package xyz.peasfultown.ecommerce.user_service.service;

import org.springframework.stereotype.Service;
import xyz.peasfultown.ecommerce.user_api.model.NewUserReq;
import xyz.peasfultown.ecommerce.user_api.model.UpdateUserReq;
import xyz.peasfultown.ecommerce.user_api.model.User;
import xyz.peasfultown.ecommerce.user_service.entity.UserEntity;
import xyz.peasfultown.ecommerce.user_service.exception.UserAlreadyExistsException;
import xyz.peasfultown.ecommerce.user_service.exception.UserNotFoundException;
import xyz.peasfultown.ecommerce.user_service.mapper.UserMapper;
import xyz.peasfultown.ecommerce.user_service.repository.UserRepository;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repo;
    private final UserMapper mapper;

    public UserServiceImpl(UserRepository repo, UserMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public User createUser(NewUserReq req) {
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
        return mapper.entityToModel(ue);
    }

    @Override
    public User updateUser(String xUserId, UpdateUserReq req) {
        UserEntity ue = repo.findById(UUID.fromString(xUserId))
                .orElseThrow(() -> new UserNotFoundException(String.format(
                        "User not found by ID: %s", xUserId
                )));

        if (Objects.nonNull(req.getEmail()))
            ue.setEmail(req.getEmail());
        if (Objects.nonNull(req.getFirstName()))
            ue.setFirstName(req.getFirstName());
        if (Objects.nonNull(req.getLastName()))
            ue.setLastName(req.getLastName());
        if (Objects.nonNull(req.getPhone()))
            ue.setPhone(req.getPhone());

        return mapper.entityToModel(repo.save(ue));
    }
}
