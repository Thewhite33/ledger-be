package com.example.ledger.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ledger.entity.User;

public interface UserRepository extends JpaRepository<User,Long> {
    
    Optional<User> findByEmail(String email); 

    boolean exiexistsByEmail(String email); 
}
