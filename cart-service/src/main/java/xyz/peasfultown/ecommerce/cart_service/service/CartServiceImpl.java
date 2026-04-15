package xyz.peasfultown.ecommerce.cart_service.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import xyz.peasfultown.ecommerce.cart_api.model.*;
import xyz.peasfultown.ecommerce.cart_service.client.ProductServiceClient;
import xyz.peasfultown.ecommerce.cart_service.entity.CartEntity;
import xyz.peasfultown.ecommerce.cart_service.entity.CartItemEntity;
import xyz.peasfultown.ecommerce.cart_service.exception.ProductOutOfStockException;
import xyz.peasfultown.ecommerce.cart_service.mapper.CartItemMapper;
import xyz.peasfultown.ecommerce.cart_service.mapper.CartMapper;
import xyz.peasfultown.ecommerce.cart_service.repository.CartItemRepository;
import xyz.peasfultown.ecommerce.cart_service.repository.CartRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CartServiceImpl implements CartService {
    private final CartRepository repo;
    private final CartItemRepository ciRepo;
    private final CartMapper mapper;
    private final CartItemMapper ciMapper;
    private final ProductServiceClient prodClient;

    public CartServiceImpl(CartRepository repo, CartItemRepository ciRepo, CartMapper mapper,
                           CartItemMapper ciMapper, ProductServiceClient prodClient) {
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

        Optional<CartItemEntity> cieo =
                ciRepo.findCartItemByCartIdAndProductId(
                        ce.getId(), UUID.fromString(req.getProductId()));

        CartItemEntity cie;
        if (cieo.isPresent()) {
            cie = cieo.get();
            cie.setQuantity(cie.getQuantity() + req.getQuantity());
            cie.setSubtotal(cie.getSubtotal().add(prod.getPrice().multiply(BigDecimal.valueOf(req.getQuantity()))));
        } else {
            cie = CartItemEntity.builder()
                    .cart(ce)
                    .productId(UUID.fromString(req.getProductId()))
                    .productName(prod.getName())
                    .productPrice(prod.getPrice())
                    .quantity(req.getQuantity())
                    .subtotal(prod.getPrice().multiply(BigDecimal.valueOf(req.getQuantity())))
                    .build();
        }

        ce.setTotalItems(ce.getTotalItems() + req.getQuantity());
        ce.setTotalPrice(ce.getTotalPrice().add(prod.getPrice().multiply(BigDecimal.valueOf(req.getQuantity()))));

        cie = ciRepo.save(cie);
        ce.getItems().add(cie);
        repo.save(ce);
        return ciMapper.toModel(cie);
    }

    private CartEntity getCartEntityByUserId(String userId) {
        Optional<CartEntity> ceo = repo.findCartByUserId(UUID.fromString(userId));

        CartEntity cart;
        if (ceo.isEmpty()) {
            cart = CartEntity.builder()
                    .userId(UUID.fromString(userId))
                    .build();
            cart = repo.save(cart);
        } else {
            cart = ceo.get();
            if (!cart.getItems().isEmpty()) {
                List<ProductId> productIds = cart.getItems().stream().map(i ->
                        new ProductId().id(i.getProductId().toString())).toList();
                List<Product> products = getProductsByIds(productIds);
                updateCartItemAndProductLinks(cart.getItems(), products);
                calculateCartEntityItemCountAndTotalPrice(cart);
                ciRepo.saveAll(cart.getItems());
                cart = repo.save(cart);
            }
        }
        return repo.save(cart);
    }

    private Product getProductById(String productId) {
        ResponseEntity<Product> res = prodClient.getProductById(productId);
        Product prod = res.getBody();

        if (prod.getStockStatus().equals(ProductStockStatus.OUT_OF_STOCK))
            throw new ProductOutOfStockException(prod.getId());

        return prod;
    }

    private List<Product> getProductsByIds(List<ProductId> productIds) {
        // TODO: maybe add checks for products that were made inactive/out of stock
        ResponseEntity<List<Product>> res = prodClient.getProductsByIds(productIds);
        return res.getBody();
    }

    private void updateCartItemAndProductLinks(
            List<CartItemEntity> cartItems,
            List<Product> products
    ) {
        cartItems.forEach(i -> {
            Product matchingProduct = products.stream().filter(p ->
                    p.getId().equals(i.getProductId().toString())).findFirst().get();
            if (!i.getProductName().equals(matchingProduct.getName())) {
                i.setProductName(matchingProduct.getName());
            }

            if (!i.getProductPrice().equals(matchingProduct.getPrice())) {
                i.setProductPrice(matchingProduct.getPrice());
                i.setSubtotal(i.getProductPrice().multiply(BigDecimal.valueOf(i.getQuantity())));
            }
        });
    }

    private void calculateCartEntityItemCountAndTotalPrice(CartEntity ce) {
        int itemCount = ce.getItems().stream()
                .mapToInt(CartItemEntity::getQuantity).sum();
        ce.setTotalItems(itemCount);
        ce.setTotalPrice(BigDecimal.ZERO);
        ce.getItems().forEach(i -> {
            ce.setTotalPrice(ce.getTotalPrice().add(i.getProductPrice().multiply(BigDecimal.valueOf(i.getQuantity()))));
        });
    }
}
