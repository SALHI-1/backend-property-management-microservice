package com.lsiproject.app.propertymanagementmicroservice.repository;

import com.lsiproject.app.propertymanagementmicroservice.entities.Property;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property,Integer> {
}
