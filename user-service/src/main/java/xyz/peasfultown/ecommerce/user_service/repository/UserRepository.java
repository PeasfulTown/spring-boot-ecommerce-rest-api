package xyz.peasfultown.ecommerce.user_service.repository;

import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import xyz.peasfultown.ecommerce.user_service.entity.UserEntity;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    @Query("""
            select u from UserEntity u where u.email = :email
            """)
    Optional<UserEntity> findUserByEmail(@Email String email);
}
