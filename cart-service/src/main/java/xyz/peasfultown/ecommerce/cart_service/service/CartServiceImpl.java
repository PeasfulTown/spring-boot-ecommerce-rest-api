package xyz.peasfultown.ecommerce.cart_service.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import xyz.peasfultown.ecommerce.cart_api.model.AddItemReq;
import xyz.peasfultown.ecommerce.cart_api.model.Cart;
import xyz.peasfultown.ecommerce.cart_api.model.CartItem;
import xyz.peasfultown.ecommerce.cart_api.model.Product;
import xyz.peasfultown.ecommerce.cart_service.client.ProductServiceClient;
import xyz.peasfultown.ecommerce.cart_service.entity.CartEntity;
import xyz.peasfultown.ecommerce.cart_service.entity.CartItemEntity;
import xyz.peasfultown.ecommerce.cart_service.exception.ProductNotFoundException;
import xyz.peasfultown.ecommerce.cart_service.mapper.CartItemMapper;
import xyz.peasfultown.ecommerce.cart_service.mapper.CartMapper;
import xyz.peasfultown.ecommerce.cart_service.repository.CartItemRepository;
import xyz.peasfultown.ecommerce.cart_service.repository.CartRepository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class CartServiceImpl implements CartService {
    private final CartRepository repo;
    private final CartItemRepository ciRepo;
    private final CartMapper mapper;
    private final CartItemMapper ciMapper;
    private final ProductServiceClient prodClient;

    public CartServiceImpl(CartRepository repo, CartItemRepository ciRepo, CartMapper mapper, CartItemMapper ciMapper, ProductServiceClient prodClient) {
        this.repo = repo;
        this.ciRepo = ciRepo;
        this.mapper = mapper;
        this.ciMapper = ciMapper;
        this.prodClient = prodClient;
    }

    @Override
    public Cart getCartByUserId(String userId) {
        CartEntity ce = getCartEntityByUserId(userId);

        return mapper.toModel(ce);
    }

    @Override
    public CartItem addItemToCart(String userId, AddItemReq req) {
        CartEntity ce = getCartEntityByUserId(userId);

        Product prod = getProductById(req.getProductId());

        CartItemEntity cie = CartItemEntity.builder()
                .cart(ce)
                .productId(UUID.fromString(req.getProductId()))
                .productName(prod.getName())
                .productPrice(prod.getPrice())
                .quantity(req.getQuantity())
                .subtotal(prod.getPrice().multiply(BigDecimal.valueOf(req.getQuantity())))
                .build();
        cie = ciRepo.save(cie);
        return ciMapper.toModel(cie);
    }

    private CartEntity getCartEntityByUserId(String userId) {
        Optional<CartEntity> ce = repo.findCartByUserid(UUID.fromString(userId));

        if (ce.isEmpty()) {
            CartEntity newCart = CartEntity.builder()
                    .userId(UUID.fromString(userId))
                    .build();
            newCart = repo.save(newCart);
            ce = Optional.of(newCart);
        }

        return ce.get();
    }

    private Product getProductById(String productId) {
        ResponseEntity<Product> res = prodClient.getProductById(productId);

        if (res.getStatusCode().equals(HttpStatus.NOT_FOUND))
            throw new ProductNotFoundException(productId);

        return res.getBody();
    }
}
