package xyz.peasfultown.ecommerce.user_service.service;

import org.springframework.data.domain.Page;
import xyz.peasfultown.ecommerce.user_api.model.*;

public interface UserService {
    User createUser(UserCreateRequest createReq);

    User updateUser(String userId, UserUpdateRequest updateReq);

    User getUser(String userId);

    void deleteUser(String userId);

    Page<User> getUsersPaged(int pageNumber, int pageSize);

    OrderInformation getOrderInfo(OrderInformationRequest orderInformationRequest);
}
