package com.lsiproject.app.propertymanagementmicroservice.CreationDTOs;

import jakarta.validation.constraints.NotNull;

public record RoomImageCreationDTO(
        @NotNull int orderIndex
) {
}
