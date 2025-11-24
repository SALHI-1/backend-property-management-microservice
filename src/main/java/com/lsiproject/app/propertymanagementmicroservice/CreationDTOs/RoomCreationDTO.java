package com.lsiproject.app.propertymanagementmicroservice.CreationDTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Data Transfer Object for creating a Room, which includes its images.
 */
public record RoomCreationDTO(

        @NotBlank String name,

        @NotNull Integer orderIndex,

        List<Integer> imageIndexes
) {}