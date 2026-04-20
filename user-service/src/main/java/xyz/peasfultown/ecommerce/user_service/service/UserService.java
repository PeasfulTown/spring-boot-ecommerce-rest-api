package xyz.peasfultown.ecommerce.user_service.service;

import xyz.peasfultown.ecommerce.user_api.model.NewUserReq;
import xyz.peasfultown.ecommerce.user_api.model.UpdateUserReq;
import xyz.peasfultown.ecommerce.user_api.model.User;

import java.util.List;

public interface UserService {
    User createUser(NewUserReq newUserReq);

    User updateUser(String xUserId, UpdateUserReq updateUserReq);

    User getUser(String xUserId);

    void deleteUserById(String xUserId);

    List<User> getAllUsers();
}
