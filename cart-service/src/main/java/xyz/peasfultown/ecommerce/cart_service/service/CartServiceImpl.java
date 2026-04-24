package xyz.peasfultown.ecommerce.cart_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.peasfultown.ecommerce.cart_api.model.*;
import xyz.peasfultown.ecommerce.cart_service.client.ProductServiceClient;
import xyz.peasfultown.ecommerce.cart_service.config.RabbitMqConstants;
import xyz.peasfultown.ecommerce.cart_service.dto.BatchProductIdRequest;
import xyz.peasfultown.ecommerce.cart_service.dto.OrderCreateMessage;
import xyz.peasfultown.ecommerce.cart_service.entity.CartEntity;
import xyz.peasfultown.ecommerce.cart_service.entity.CartItemEntity;
import xyz.peasfultown.ecommerce.cart_service.exception.*;
import xyz.peasfultown.ecommerce.cart_service.mapper.CartItemMapper;
import xyz.peasfultown.ecommerce.cart_service.mapper.CartMapper;
import xyz.peasfultown.ecommerce.cart_service.messaging.MessagePublisher;
import xyz.peasfultown.ecommerce.cart_service.repository.CartItemRepository;
import xyz.peasfultown.ecommerce.cart_service.repository.CartRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartServiceImpl implements CartService {
    private final CartRepository repo;
    private final CartItemRepository ciRepo;
    private final CartMapper mapper;
    private final CartItemMapper ciMapper;
    private final ProductServiceClient prodClient;
    private final MessagePublisher messagePublisher;

    public CartServiceImpl(CartRepository repo, CartItemRepository ciRepo, CartMapper mapper,
                           CartItemMapper ciMapper, ProductServiceClient prodClient, MessagePublisher messagePublisher) {
        this.repo = repo;
        this.ciRepo = ciRepo;
        this.mapper = mapper;
        this.ciMapper = ciMapper;
        this.prodClient = prodClient;
        this.messagePublisher = messagePublisher;
    }

    @Override
    public Cart getCartByUserId(String userId) {
        CartEntity ce = getCartEntityByUserId(userId);
        List<Product> cartItemProducts = getProductsByIds(
                ce.getItems().stream().map(i ->
                        i.getProductId().toString()).toList());

        Map<String, Product> productMap = createProductMap(cartItemProducts);

        // if the resulting list size of the get product call
        // is less than the size of the items in the cart
        // that means some items in the cart have been made
        // unavailable or out of stock, in that case the cart
        // items list will be updated to get rid of those items
        if (ce.getItems().size() > cartItemProducts.size())
            updateCartItemAndProductLinks(ce, productMap);

        return mapper.toModel(ce, productMap);
    }

    @Override
    public CartItem addItemToCart(String userId, ItemCreateRequest req) {
        CartEntity ce = getCartEntityByUserId(userId);

        Product product;
        try {
            product = getProductById(req.getProductId());
        } catch (
                FeignProductNotFoundException e) {
            throw new ProductNotFoundException(req.getProductId());
        }

        // if product not active or out of stock, throw exception
        if (product.getStockStatus().equals(StockStatus.OUT_OF_STOCK))
            throw new ProductOutOfStockException(product.getId());

        if (product.getActiveStatus().equals(ActiveStatus.INACTIVE))
            throw new ProductNotFoundException(product.getId());

        Optional<CartItemEntity> cieo =
                ciRepo.findCartItemByCartIdAndProductId(
                        ce.getId(), UUID.fromString(req.getProductId()));

        // TODO: check if product has enough stock for the request quantity
        CartItemEntity cie;
        if (cieo.isPresent()) {
            cie = cieo.get();
            cie.setQuantity(cie.getQuantity() + req.getQuantity());
        } else {
            cie = CartItemEntity.builder()
                    .cart(ce)
                    .productId(UUID.fromString(req.getProductId()))
                    .quantity(req.getQuantity())
                    .build();
        }
        cie = ciRepo.save(cie);
        return ciMapper.toModel(cie, product);
    }

    @Override
    public void clearCart(String userId) {
        // don't need to use get user cart private function which updates cart items
        // since the cart is being cleared anyway
        CartEntity ce = repo.findCartByUserId(UUID.fromString(userId))
                .orElseThrow(() -> new CartNotFoundException(String.format(
                        "Cart not found by user ID: %s", userId
                )));
        ciRepo.deleteAll(ce.getItems());
    }

    @Override
    public void removeItemFromCart(String userId, String itemId) {
        CartEntity ce = repo.findCartByUserId(UUID.fromString(userId))
                .orElseThrow(() -> new CartNotFoundException(String.format(
                        "Cart not found by user ID: %s", userId
                )));

        CartItemEntity cie = ce.getItems().stream().filter(ci -> ci.getId().equals(UUID.fromString(itemId))).findFirst().get();
        ciRepo.delete(cie);
    }

    @Override
    public CartItem updateCartItemQuantity(String userId, String itemId, ItemQuantityUpdateRequest req) {
        CartEntity ce = repo.findCartByUserId(UUID.fromString(userId))
                .orElseThrow(() -> new CartNotFoundException(String.format(
                        "Cart not found by user ID: %s", userId
                )));

        CartItemEntity cie = ce.getItems().stream()
                .filter(i -> i.getId().toString().equals(itemId))
                .findFirst().orElseThrow(() -> new CartItemNotFoundException(String.format(
                        "Cart item not found by ID: %s", itemId
                )));

        Product product;
        try {
            product = getProductById(cie.getProductId().toString());
        } catch (
                FeignProductNotFoundException e) {
            ciRepo.delete(cie);
            throw new ProductNotFoundException(cie.getProductId().toString());
        }

        cie.setQuantity(req.getQuantity());
        cie = ciRepo.save(cie);
        return ciMapper.toModel(cie, product);
    }

    @Override
    public void checkoutCart(String userId, CartCheckoutRequest cartCheckoutReq) {
        Cart cart = getCartByUserId(userId);

        if (cart.getItems().isEmpty())
            throw new EmptyCartCheckoutException("Cannot checkout with an empty cart");

        OrderCreateMessage orderCreateMessage = OrderCreateMessage.builder()
                .userId(userId)
                .cardId(cartCheckoutReq.getCardId())
                .addressId(cartCheckoutReq.getAddressId())
                .items(cart.getItems())
                .build();

        messagePublisher.sendOrderCreateMessage(orderCreateMessage);

        ciRepo.deleteAllByIdInBatch(cart.getItems().stream().map(i ->
                UUID.fromString(i.getId())).toList());
    }


    private CartEntity getCartEntityByUserId(String userId) {
        Optional<CartEntity> ceo = repo.findCartByUserId(UUID.fromString(userId));

        CartEntity cart;
        if (ceo.isEmpty()) {
            cart = CartEntity.builder()
                    .userId(UUID.fromString(userId))
                    .build();
            cart = repo.save(cart);
        } else
            cart = ceo.get();

        return cart;
    }

    private Product getProductById(String productId) {
        ResponseEntity<Product> res = prodClient.getProductById(productId);
        return res.getBody();
    }

    private List<Product> getProductsByIds(List<String> productIds) {
        // TODO: maybe add checks for products that were made inactive/out of stock
        ResponseEntity<List<Product>> res = prodClient.getProductsByIds(new BatchProductIdRequest(productIds));
        List<Product> products = res.getBody();

        return products;
    }

    private Map<String, Product> createProductMap(List<Product> products) {
        return products.stream().collect(Collectors.toMap(Product::getId, Function.identity()));
    }

    private void updateCartItemAndProductLinks(CartEntity cartEntity, Map<String, Product> productMap) {
        cartEntity.setItems(cartEntity.getItems().stream().filter(i ->
                        Optional.ofNullable(
                                productMap.get(i.getProductId().toString())).isPresent())
                .toList());
        repo.save(cartEntity);
    }
}
