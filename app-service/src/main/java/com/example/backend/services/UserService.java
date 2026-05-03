package com.example.backend.services;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.hibernate.engine.jdbc.proxy.BlobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.models.Address;
import com.example.backend.models.User;
import com.example.backend.repositories.AddressRepository;
import com.example.backend.repositories.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public void registerNewUser(String username, String email, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setEncodedPassword(passwordEncoder.encode(password));
        user.setRoles(Arrays.asList("ROLE_USER"));
        userRepository.save(user);
    }

    public void updateProfile(String oldUsername, String newUsername, String email, MultipartFile imageFile) throws IOException {
        userRepository.findByUsername(oldUsername).ifPresent(user -> {
            user.setUsername(newUsername);
            user.setEmail(email);

            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    user.setProfilePicture(BlobProxy.generateProxy(imageFile.getInputStream(), imageFile.getSize()));
                    user.setHasPicture(true);
                } catch (IOException e) {
                    // Log error or handle
                }
            }
            userRepository.save(user);
        });
    }

    public boolean deleteAddressWithCheck(Long addressId, String username, boolean isAdmin) {
        Optional<Address> optAddr = addressRepository.findById(addressId);
        if (optAddr.isEmpty()) return false;

        Address addr = optAddr.get();
        if (isAdmin || (addr.getUser() != null && addr.getUser().getUsername().equals(username))) {
            addressRepository.deleteById(addressId);
            return true;
        }
        return false;
    }
}