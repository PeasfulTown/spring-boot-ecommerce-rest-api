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
    public ResponseEntity<Card> createPaymentCard(String userIdHeader, CardCreateRequest cardCreateRequest) throws Exception {
        return status(HttpStatus.CREATED).body(service.createPaymentCard(userIdHeader, cardCreateRequest));
    }

    @Override
    public ResponseEntity<Card> getPaymentCard(String userIdHeader, String cardId) throws Exception {
        return ok(service.getCard(cardId));
    }

    @Override
    public ResponseEntity<List<Card>> getPaymentCards(String userIdHeader) throws Exception {
        return ok(service.getUserCards(userIdHeader));
    }

    @Override
    public ResponseEntity<Void> setPaymentCardAsDefault(String xUserId, String cardId) throws Exception {
        service.setCardAsDefault(xUserId, cardId);
        return status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<Void> deletePaymentCard(String cardId) throws Exception {
        service.deleteCard(cardId);
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
