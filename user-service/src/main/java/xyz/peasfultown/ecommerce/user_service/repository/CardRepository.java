package xyz.peasfultown.ecommerce.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import xyz.peasfultown.ecommerce.user_service.entity.CardEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardRepository extends JpaRepository<CardEntity, UUID> {
    @Query("""
            select c from CardEntity c
            where c.user.id = :userId
            and c.id = :cardId
            """)
    Optional<CardEntity> findCardByUserIdAndCardId(UUID userId, UUID cardId);

    @Query("""
            select c from CardEntity c
            where c.user.id = :userId
            """)
    List<CardEntity> findCardsByUserId(UUID userId);
}
