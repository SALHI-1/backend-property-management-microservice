package com.lsiproject.app.propertymanagementmicroservice.UpdateDTOs;

import com.lsiproject.app.propertymanagementmicroservice.Enums.PropertyType;
import com.lsiproject.app.propertymanagementmicroservice.Enums.TypeOfRental;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for updating an existing Property listing.
 * Includes 'isAvailable' which is critical for the on-chain update call.
 */
public record PropertyUpdateDTO(
        // Descriptive Fields
         String title,
         String country,
         String city,
         String address,
         Double longitude,
         Double latitude,
         String description,
         Integer SqM,
         PropertyType typeOfProperty,
         TypeOfRental typeOfRental,
         Integer total_Rooms,
         Long rentAmount,
         Long securityDeposit,
         Boolean isAvailable
) {
    public String fullAddress() {
        return country + ", " + city + ", " + address;
    }
}