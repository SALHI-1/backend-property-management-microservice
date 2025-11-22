package com.lsiproject.app.propertymanagementmicroservice.DTOs;

import com.lsiproject.app.propertymanagementmicroservice.Enums.TypeOfRental;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for creating a new Property listing.
 */
public record PropertyCreationDTO(
        @NotBlank String title,
        @NotBlank String country,
        @NotBlank String city,
        @NotBlank String propertyAddress, // Maps to contract's propertyAddress
        @NotBlank String description,
        @NotNull TypeOfRental typeOfRental,
        @NotNull Long rentPerMonth, // In minor units (e.g., cents/wei)
        @NotNull Long securityDeposit
) {}