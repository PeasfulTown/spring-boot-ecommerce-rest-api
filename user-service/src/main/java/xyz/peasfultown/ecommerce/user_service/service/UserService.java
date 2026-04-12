package xyz.peasfultown.ecommerce.user_service.service;

import xyz.peasfultown.ecommerce.user_api.model.NewUserReq;
import xyz.peasfultown.ecommerce.user_api.model.UpdateUserReq;
import xyz.peasfultown.ecommerce.user_api.model.User;

public interface UserService {
    User createUser(NewUserReq newUserReq);

    User updateUser(String xUserId, UpdateUserReq updateUserReq);
}
