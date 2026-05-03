package com.example.backend.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.models.User;
import com.example.backend.repositories.UserRepository;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Long getCurrentUserId(String username) {
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElse(null);
    }
}