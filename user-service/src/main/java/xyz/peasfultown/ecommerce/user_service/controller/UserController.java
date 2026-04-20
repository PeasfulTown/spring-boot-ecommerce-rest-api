package xyz.peasfultown.ecommerce.user_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import xyz.peasfultown.ecommerce.user_api.UserApi;
import xyz.peasfultown.ecommerce.user_api.model.NewUserReq;
import xyz.peasfultown.ecommerce.user_api.model.UpdateUserReq;
import xyz.peasfultown.ecommerce.user_api.model.User;
import xyz.peasfultown.ecommerce.user_service.exception.ForbiddenException;
import xyz.peasfultown.ecommerce.user_service.service.UserService;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
public class UserController implements UserApi {
    private final UserService service;

    @Autowired
    public UserController(UserService service) {
        this.service = service;
    }

    // USER

    @Override
    public ResponseEntity<User> createUser(NewUserReq newUserReq) throws Exception {
        return status(HttpStatus.CREATED).body(service.createUser(newUserReq));
    }

    @Override
    public ResponseEntity<User> updateMyUser(String xUserId, UpdateUserReq updateUserReq) throws Exception {
        return ok(service.updateUser(xUserId, updateUserReq));
    }

    @Override
    public ResponseEntity<User> getMyUser(String xUserId) throws Exception {
        return ok(service.getUser(xUserId));
    }

    @Override
    public ResponseEntity<Void> deleteMyUser(String xUserId) throws Exception {
        service.deleteUserById(xUserId);
        return status(HttpStatus.NO_CONTENT).build();
    }

    // ADMIN

    @Override
    public ResponseEntity<Void> deleteUser(String xUserRole, String id) throws Exception {
        if (!xUserRole.equalsIgnoreCase("ADMIN"))
            throw new ForbiddenException();

        service.deleteUserById(id);
        return status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<List<User>> getUsers(String xUserRole) throws Exception {
        if (!xUserRole.equalsIgnoreCase("ADMIN"))
            throw new ForbiddenException();

        return ok(service.getAllUsers());
    }
}
