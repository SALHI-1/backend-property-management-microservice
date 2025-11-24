package com.lsiproject.app.propertymanagementmicroservice.ResponseDTOs;

public record RoomImageResponseDTO(
        Long idImage,
        String url,
        String s3Key,
        Integer orderIndex,
        Long roomId
) {}
