package com.example.bank_app.repository;

import com.example.bank_app.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountEntity, String> {

    Optional<AccountEntity> findByAccountNumber(String accountNumber);

    Optional<AccountEntity> findByIdAndCustomerId(String accountId, String customerId);

    List<AccountEntity> findAllByCustomerId(String customerId);
}
