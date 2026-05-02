package xyz.peasfultown.ecommerce.user_service.service;

import xyz.peasfultown.ecommerce.user_api.model.Card;
import xyz.peasfultown.ecommerce.user_api.model.CardCreateRequest;
import xyz.peasfultown.ecommerce.user_api.model.CardToken;

import java.util.List;

public interface CardService {
    Card createPaymentCard(String userId, CardCreateRequest req);

    Card getCard(String userId, String cardId);

    List<Card> getCards(String userId);

    void setCardAsDefault(String userId, String cardId);

    void deleteCard(String userId, String cardId);

    CardToken getCardToken(String cardId);
}
