package com.example.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.example.backend.models.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserId(Long userId);
}
