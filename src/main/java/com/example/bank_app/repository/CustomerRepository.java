package com.example.bank_app.repository;

import com.example.bank_app.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<CustomerEntity, String> {

    Optional<CustomerEntity> findByEmail(String email);
}
