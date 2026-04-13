package xyz.peasfultown.ecommerce.cart_service.service;

import org.springframework.stereotype.Service;
import xyz.peasfultown.ecommerce.cart_api.model.Cart;
import xyz.peasfultown.ecommerce.cart_service.entity.CartEntity;
import xyz.peasfultown.ecommerce.cart_service.mapper.CartMapper;
import xyz.peasfultown.ecommerce.cart_service.repository.CartRepository;

import java.util.Optional;
import java.util.UUID;

@Service
public class CartServiceImpl implements CartService {
    private final CartRepository repo;
    private final CartMapper mapper;

    public CartServiceImpl(CartRepository repo, CartMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public Cart getCartByUserId(String userId) {
        Optional<CartEntity> ce = repo.findCartByUserid(UUID.fromString(userId));

        if (ce.isEmpty()) {
            CartEntity newCart = CartEntity.builder()
                    .userId(UUID.fromString(userId))
                    .build();
            newCart = repo.save(newCart);
            ce = Optional.of(newCart);
        }

        return mapper.toModel(ce.get());
    }
}
