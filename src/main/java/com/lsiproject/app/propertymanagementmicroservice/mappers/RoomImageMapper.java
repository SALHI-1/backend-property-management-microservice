package com.lsiproject.app.propertymanagementmicroservice.mappers;

import com.lsiproject.app.propertymanagementmicroservice.ResponseDTOs.RoomImageResponseDTO;
import com.lsiproject.app.propertymanagementmicroservice.entities.RoomImage;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting RoomImage entities to RoomImageResponseDTOs.
 */
@Component
public class RoomImageMapper {

    public RoomImageResponseDTO toDto(RoomImage entity) {
        if (entity == null) {
            return null;
        }

        return new RoomImageResponseDTO(
                entity.getIdImage(),
                entity.getUrl(),
                entity.getS3Key(),
                entity.getOrderIndex(),
                // Safely access the parent room ID
                entity.getRoom() != null ? entity.getRoom().getIdRoom() : null
        );
    }
}