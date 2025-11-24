package com.lsiproject.app.propertymanagementmicroservice.ResponseDTOs;

import java.util.List;

public record RoomResponseDTO(
        Long idRoom,
        String name,
        Integer orderIndex,
        Long propertyId
) {}
