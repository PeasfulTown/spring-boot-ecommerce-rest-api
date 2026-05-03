package xyz.peasfultown.ecommerce.user_service.service;

import xyz.peasfultown.ecommerce.user_api.model.Card;
import xyz.peasfultown.ecommerce.user_api.model.CardCreateRequest;
import xyz.peasfultown.ecommerce.user_api.model.CardToken;

import java.util.List;

public interface CardService {
    Card createPaymentCard(String userId, CardCreateRequest req);

    Card getCard(String cardId);

    List<Card> getUserCards(String userId);

    void setCardAsDefault(String userId, String cardId);

    void deleteCard(String cardId);

    CardToken getCardToken(String cardId);
}
