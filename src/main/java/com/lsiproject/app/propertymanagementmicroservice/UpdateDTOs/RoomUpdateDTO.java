package com.lsiproject.app.propertymanagementmicroservice.UpdateDTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for updating a Room's name and order index.
 */
public record RoomUpdateDTO(
        @NotBlank String name,
        @NotNull Integer orderIndex
) {}