package xyz.peasfultown.ecommerce.auth_service.repository;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import xyz.peasfultown.ecommerce.auth_service.entity.AccountEntity;

import java.util.Optional;
import java.util.UUID;

public interface AuthRepository extends JpaRepository<AccountEntity, UUID> {
    Optional<AccountEntity> findAccountByEmail(@NotNull @Email String email);
}
