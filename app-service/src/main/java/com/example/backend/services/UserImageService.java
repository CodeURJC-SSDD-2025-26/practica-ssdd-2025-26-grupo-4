package com.example.backend.services;

import java.sql.SQLException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.example.backend.models.User;
import com.example.backend.repositories.UserRepository;

@Service
public class UserImageService {

    @Autowired
    private UserRepository userRepository;

    public Optional<Resource> getUserImage(long id) throws SQLException {

        Optional<User> user = userRepository.findById(id);

        if (user.isPresent() && user.get().getProfilePicture() != null) {
            Resource file = new InputStreamResource(
                    user.get().getProfilePicture().getBinaryStream()
            );
            return Optional.of(file);
        }

        return Optional.empty();
    }
}