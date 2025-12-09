package com.lsiproject.app.propertymanagementmicroservice.CreationDTOs;

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
        @NotNull TypeOfRental typeOfRental,
        @NotNull Long rentAmount,
        @NotNull Long securityDeposit
//        @NotNull(message = "The property must include at least one room definition.")
//        @Size(min = 1, message = "At least one room definition is required.")
//        List<RoomCreationDTO> rooms
) {

    public String fullAddress() {
        return country + ", " + city + ", " + address;
    }
}