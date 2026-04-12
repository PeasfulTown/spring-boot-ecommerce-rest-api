package xyz.peasfultown.ecommerce.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import xyz.peasfultown.ecommerce.user_service.entity.AddressEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<AddressEntity, UUID> {
    @Query("""
            select a from AddressEntity a where a.id = :addressId and a.user.id = :userId
            """)
    Optional<AddressEntity> findAddressByUserAndId(UUID userId, UUID addressId);

    @Query("""
            select a from AddressEntity a where a.user.id = :userId
            """)
    List<AddressEntity> findAddressesByUserId(UUID userId);
}
