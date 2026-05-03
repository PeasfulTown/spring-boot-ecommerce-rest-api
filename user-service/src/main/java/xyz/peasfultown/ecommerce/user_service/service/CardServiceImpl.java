package xyz.peasfultown.ecommerce.user_service.service;

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.peasfultown.ecommerce.user_api.model.Card;
import xyz.peasfultown.ecommerce.user_api.model.CardCreateRequest;
import xyz.peasfultown.ecommerce.user_api.model.CardToken;
import xyz.peasfultown.ecommerce.user_api.model.CardType;
import xyz.peasfultown.ecommerce.user_service.entity.CardEntity;
import xyz.peasfultown.ecommerce.user_service.entity.UserEntity;
import xyz.peasfultown.ecommerce.user_service.exception.CardExpiredException;
import xyz.peasfultown.ecommerce.user_service.exception.CardNotFoundException;
import xyz.peasfultown.ecommerce.user_service.exception.UserNotFoundException;
import xyz.peasfultown.ecommerce.user_service.mapper.CardMapper;
import xyz.peasfultown.ecommerce.user_service.repository.CardRepository;
import xyz.peasfultown.ecommerce.user_service.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepo;
    private final UserRepository userRepo;
    private final CardMapper cardMapper;

    @Autowired
    public CardServiceImpl(CardRepository cardRepo, UserRepository userRepo, CardMapper cardMapper) {
        this.cardRepo = cardRepo;
        this.userRepo = userRepo;
        this.cardMapper = cardMapper;
    }

    @Override
    public Card createPaymentCard(String userId, CardCreateRequest req) {
        UserEntity ue = userRepo.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UserNotFoundException(String.format(
                        "User not found by ID: %s", userId
                )));

        String lastFour = req.getCardNumber().substring(req.getCardNumber().length() - 4);

        CardEntity.CardType cardType = detectCardType(req.getCardNumber());

        String token = generateToken(req.getCardNumber(),
                req.getCvv(),
                req.getExpiryMonth(),
                req.getExpiryYear(),
                cardType
                );


        CardEntity ce = CardEntity.builder()
                .user(ue)
                .cardHolderName(req.getCardHolderName())
                .lastFourDigits(lastFour)
                .cardType(cardType)
                .expiryMonth(req.getExpiryMonth())
                .expiryYear(req.getExpiryYear())
                .token(token)
                .build();
        cardRepo.save(ce);
        return cardMapper.toModel(ce);
    }

    @Override
    public Card getCard(String cardId) {
        CardEntity ce = cardRepo.findById(UUID.fromString(cardId))
                .orElseThrow(() -> new CardNotFoundException(String.format(
                        "Card not found by ID: %s", cardId
                )));

        return cardMapper.toModel(ce);
    }

    @Override
    public List<Card> getUserCards(String userId) {
        List<CardEntity> ces = cardRepo.findCardsByUserId(UUID.fromString(userId));

        return cardMapper.toModel(ces);
    }

    @Override
    public void setCardAsDefault(String userId, String cardId) {
        List<CardEntity> ces = cardRepo.findCardsByUserId(UUID.fromString(userId));

        ces.forEach(c -> {
            if (c.getId().toString().equals(cardId))
                c.setDefault(true);
            else
                c.setDefault(false);
        });
        cardRepo.saveAll(ces);
    }

    @Override
    public void deleteCard(String cardId) {
        CardEntity ce = cardRepo.findById(UUID.fromString(cardId))
                .orElseThrow(() -> new CardNotFoundException(String.format(
                        "Card not found by ID: %s", cardId
                )));

        cardRepo.delete(ce);
    }

    public String generateToken(String cardNumber,
                                Integer cvv,
                                Integer expiryMonth,
                                Integer expiryYear,
                                CardEntity.CardType cardType) {
        String lastFour = cardNumber.substring(cardNumber.length() - 4);

        String cvvHash = Integer.toHexString(cvv.hashCode());
        String random = UUID.randomUUID().toString().substring(0, 8);
        return String.format("tok_%s_%s_%s%s_%s_%s",
                cardType.name().toLowerCase(),
                lastFour,
                String.valueOf(expiryMonth),
                String.valueOf(expiryYear),
                cvvHash,
                random
        );
    }

    private void validateCardExpiry(Integer expiryMonth, Integer expiryYear) {
        LocalDate expiry = LocalDate.of(expiryYear, expiryMonth, 1);

        if (expiry.isBefore(LocalDate.now()))
            throw new CardExpiredException("Unable to add card, card already expired");
    }

    private CardEntity.CardType detectCardType(@NotNull String cardNumber) {
        if (cardNumber.startsWith("4"))
            return CardEntity.CardType.VISA;
        if (cardNumber.startsWith("5"))
            return CardEntity.CardType.MASTERCARD;
        if (cardNumber.startsWith("3"))
            return CardEntity.CardType.AMEX;
        return CardEntity.CardType.VISA;
    }

    @Override
    public CardToken getCardToken(String cardId) {
        CardEntity ce = cardRepo.findById(UUID.fromString(cardId))
                .orElseThrow(() -> new CardNotFoundException(String.format(
                        "Card not found by ID: %s", cardId
                )));

        CardToken token = CardToken.builder()
                .cardId(ce.getId().toString())
                .token(ce.getToken())
                .lastFourDigits(ce.getLastFourDigits())
                .cardType(CardType.valueOf(ce.getCardType().getValue()))
                .expiryMonth(ce.getExpiryMonth())
                .expiryYear(ce.getExpiryYear())
                .build();

        return token;
    }
}
