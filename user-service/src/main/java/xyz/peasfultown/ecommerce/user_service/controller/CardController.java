package xyz.peasfultown.ecommerce.user_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import xyz.peasfultown.ecommerce.user_api.CardApi;
import xyz.peasfultown.ecommerce.user_api.model.Card;
import xyz.peasfultown.ecommerce.user_api.model.CardCreateRequest;
import xyz.peasfultown.ecommerce.user_api.model.CardToken;
import xyz.peasfultown.ecommerce.user_service.exception.AccessForbiddenException;
import xyz.peasfultown.ecommerce.user_service.service.CardService;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
public class CardController implements CardApi {
    @Value("${services.internal-secret}")
    private String internalSecret;

    private final CardService service;

    @Autowired
    public CardController(CardService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<Card> createPaymentCard(String xUserId, String userId, CardCreateRequest cardCreateRequest) throws Exception {
        if (!xUserId.equals(userId))
            throw new AccessForbiddenException();

        return status(HttpStatus.CREATED).body(service.createPaymentCard(userId, cardCreateRequest));
    }

    @Override
    public ResponseEntity<Card> getPaymentCard(String xUserId, String userId, String cardId) throws Exception {
        if (!xUserId.equals(userId))
            throw new AccessForbiddenException();

        return ok(service.getCard(userId, cardId));
    }

    @Override
    public ResponseEntity<List<Card>> getPaymentCards(String xUserId, String userId) throws Exception {
        if (!xUserId.equals(userId))
            throw new AccessForbiddenException();

        return ok(service.getCards(userId));
    }

    @Override
    public ResponseEntity<Void> setPaymentCardAsDefault(String xUserId, String userId, String cardId) throws Exception {
        service.setCardAsDefault(userId, cardId);
        return status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<Void> deletePaymentCard(String xUserId, String userId, String cardId) throws Exception {
        service.deleteCard(userId, cardId);
        return status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<CardToken> getPaymentCardToken(String xUserRole, String xInternalServiceSecret, String cardId) throws Exception {
        if (!xUserRole.equals("ADMIN")
        || !xInternalServiceSecret.equals(internalSecret))
            throw new AccessForbiddenException();

        return ok(service.getCardToken(cardId));
    }
}
