package com.lsiproject.app.propertymanagementmicroservice.ResponseDTOs;

import com.lsiproject.app.propertymanagementmicroservice.Enums.TypeOfRental;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;

public record PropertyResponseDTO(
        Long idProperty,
        Long onChainId,

        String title,
        String country,
        String city,
        String address,
        Double longitude,
        Double latitude,
        String description,
        TypeOfRental typeOfRental,

        Long rentAmount,
        Long securityDeposit,
        Boolean isAvailable,
        Boolean isActive,

        Long ownerId,
        String ownerEthAddress,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
