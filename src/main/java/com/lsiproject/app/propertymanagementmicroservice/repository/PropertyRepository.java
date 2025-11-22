package com.lsiproject.app.propertymanagementmicroservice.repository;

import com.lsiproject.app.propertymanagementmicroservice.entities.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PropertyRepository extends JpaRepository<Property,Long> {
    Optional<Property> findByOnChainId(Long onChainId);
}
