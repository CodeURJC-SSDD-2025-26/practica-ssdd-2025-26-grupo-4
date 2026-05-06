package com.example.backend.services;

import com.example.backend.models.Address;
import com.example.backend.models.User;
import com.example.backend.repositories.AddressRepository;
import com.example.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerNewUser(String username, String email, String password) throws Exception {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new Exception("Username already exists.");
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setEncodedPassword(passwordEncoder.encode(password));
        user.setRoles(Arrays.asList("ROLE_USER"));
        return userRepository.save(user);
    }

    public User createAdminOrUser(String username, String email, String password, boolean isAdmin)
            throws Exception {

        if (userRepository.findByUsername(username).isPresent()) {
            throw new Exception("Username already exists.");
        }

        User user = new User();

        user.setUsername(username);
        user.setEmail(email);
        user.setEncodedPassword(passwordEncoder.encode(password));

        user.setRoles(Arrays.asList(isAdmin ? "ROLE_ADMIN" : "ROLE_USER"));

        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public void deleteUser(Long id) throws Exception {
        userRepository.deleteById(id);
    }

    public void deleteAddress(Long addressId, String username, boolean isAdmin) throws Exception {
        Address addr = addressRepository.findById(addressId)
                .orElseThrow(() -> new Exception("Address not found."));

        if (isAdmin || (addr.getUser() != null && addr.getUser().getUsername().equals(username))) {
            addressRepository.deleteById(addressId);
        } else {
            throw new Exception("Unauthorized access to delete this address.");
        }
    }
}