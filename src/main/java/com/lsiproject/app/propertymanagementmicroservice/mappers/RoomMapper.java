package com.lsiproject.app.propertymanagementmicroservice.mappers;

import com.lsiproject.app.propertymanagementmicroservice.ResponseDTOs.RoomResponseDTO;
import com.lsiproject.app.propertymanagementmicroservice.entities.Room;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting Room entities to RoomResponseDTOs.
 */
@Component
public class RoomMapper {

    // NOTE: This assumes RoomImageResponseDTO is available in the DTO package.

    public RoomResponseDTO toDto(Room entity) {
        if (entity == null) {
            return null;
        }

        return new RoomResponseDTO(
                entity.getIdRoom(),
                entity.getName(),
                entity.getOrderIndex(),
                entity.getProperty().getIdProperty()
        );
    }
}