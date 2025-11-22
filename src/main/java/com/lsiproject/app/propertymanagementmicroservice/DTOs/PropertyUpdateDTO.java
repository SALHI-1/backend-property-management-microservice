package com.lsiproject.app.propertymanagementmicroservice.DTOs;

import com.lsiproject.app.propertymanagementmicroservice.Enums.TypeOfRental;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for updating an existing Property listing.
 * Includes 'isAvailable' which is critical for the on-chain update call.
 */
public record PropertyUpdateDTO(
        // Descriptive Fields
        @NotBlank String title,
        @NotBlank String country,
        @NotBlank String city,
        @NotBlank String description,

        // Rental Terms (Mandatory update for on-chain integrity)
        @NotNull TypeOfRental typeOfRental,
        @NotNull Long rentPerMonth,
        @NotNull Long securityDeposit,

        // Status Field (Mandatory for contract update)
        @NotNull Boolean isAvailable // Maps to _isAvailable in the contract
) {}