package com.lsiproject.app.propertymanagementmicroservice.CreationDTOs;

import com.lsiproject.app.propertymanagementmicroservice.Enums.PropertyType;
import com.lsiproject.app.propertymanagementmicroservice.Enums.TypeOfRental;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Data Transfer Object for creating a new Property listing.
 */
public record PropertyCreationDTO(
        @NotNull Long onChainId,
        @NotBlank String title,
        @NotBlank String country,
        @NotBlank String city,
        @NotBlank String address,
        @NotNull Double longitude,
        @NotNull Double latitude,
        @NotBlank String description,
        @NotNull Integer sqM,
        @NotNull PropertyType typeOfProperty,
        @NotNull TypeOfRental typeOfRental,
        @NotNull Long rentAmount,
        @NotNull Long securityDeposit

) {

    public String fullAddress() {
        return country + ", " + city + ", " + address;
    }
}