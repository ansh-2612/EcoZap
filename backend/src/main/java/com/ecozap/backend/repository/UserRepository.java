package com.ecozap.backend.repository;

import com.ecozap.backend.model.user;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<user, String> {

    Optional<user> findByEmail(String email);
    long count();
}