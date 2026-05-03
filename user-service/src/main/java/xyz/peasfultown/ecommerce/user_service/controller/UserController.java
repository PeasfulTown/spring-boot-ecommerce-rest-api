package xyz.peasfultown.ecommerce.user_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import xyz.peasfultown.ecommerce.user_api.UserApi;
import xyz.peasfultown.ecommerce.user_api.model.*;
import xyz.peasfultown.ecommerce.user_service.exception.AccessForbiddenException;
import xyz.peasfultown.ecommerce.user_service.service.UserService;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
public class UserController implements UserApi {
    private final UserService service;

    @Value("${services.internal-secret}")
    private String internalSecret;

    @Autowired
    public UserController(UserService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<User> createUser(String internalSecret, String xUserRole, UserCreateRequest req) throws Exception {
        if (!internalSecret.equals(this.internalSecret)
            || !xUserRole.equals("ADMIN"))
            throw new AccessForbiddenException();
        return status(HttpStatus.CREATED).body(service.createUser(req));
    }

    @Override
    public ResponseEntity<PagedUserResponse> getUsers(String userRoleHeader, Integer pageNumber, Integer pageSize) throws Exception {
        if (!userRoleHeader.equalsIgnoreCase("ADMIN"))
            throw new AccessForbiddenException();

        if (pageNumber == null)
            pageNumber = 0;
        if (pageSize == null)
            pageSize = 10;

        Page<User> userPage = service.getUsersPaged(pageNumber, pageSize);
        PagedUserResponse res = PagedUserResponse.builder()
                .content(userPage.getContent())
                .page(ResponsePage.builder()
                        .number(userPage.getNumber())
                        .size(userPage.getSize())
                        .totalElements(userPage.getTotalElements())
                        .totalPages(userPage.getTotalPages())
                        .build())
        .build();

        return ok(res);
    }


    @Override
    public ResponseEntity<User> getUser(String userIdHeader) throws Exception {
        return ok(service.getUser(userIdHeader));
    }

    @Override
    public ResponseEntity<User> adminGetUser(String userRoleHeader, String userIdPath) throws Exception {
        if (!userRoleHeader.equals("ADMIN"))
            throw new AccessForbiddenException();

        return ok(service.getUser(userIdPath));
    }

    @Override
    public ResponseEntity<User> updateUser(String userIdHeader, UserUpdateRequest req) throws Exception {
        return ok(service.updateUser(userIdHeader, req));
    }

    @Override
    public ResponseEntity<User> adminUpdateUser(String xUserRole, String userId, UserUpdateRequest userUpdateRequest) throws Exception {
        if (!xUserRole.equals("ADMIN"))
            throw new AccessForbiddenException();
        return updateUser(userId, userUpdateRequest);
    }

    @Override
    public ResponseEntity<Void> deleteUser(String userIdPath) throws Exception {
        service.deleteUser(userIdPath);
        return status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<Void> adminDeleteUser(String xUserRole, String userId) throws Exception {
        if (!xUserRole.equals("ADMIN"))
            throw new AccessForbiddenException();
        return deleteUser(userId);
    }

    @Override
    public ResponseEntity<OrderInformation> getOrderInfo(String userRoleHeader, String internalSecret, OrderInformationRequest orderInformationRequest) throws Exception {
        if (!internalSecret.equals(this.internalSecret)
            || !userRoleHeader.equals("ADMIN"))
            throw new AccessForbiddenException();
        return ok(service.getOrderInfo(orderInformationRequest));
    }
}
