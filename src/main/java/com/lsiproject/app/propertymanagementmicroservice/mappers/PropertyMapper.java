package com.lsiproject.app.propertymanagementmicroservice.mappers;


import com.lsiproject.app.propertymanagementmicroservice.ResponseDTOs.PropertyResponseDTO;
import com.lsiproject.app.propertymanagementmicroservice.entities.Property;
import org.springframework.stereotype.Component;

/**
 * Handles the mapping from JPA Entities (internal model) to Response DTOs (API contract).
 * This component breaks the infinite recursion loop by selectively mapping fields.
 */
@Component
public class PropertyMapper {

    public PropertyResponseDTO toDto(Property entity) {
        if (entity == null) {
            return null;
        }

        return new PropertyResponseDTO(
                entity.getIdProperty(),
                entity.getOnChainId(),
                entity.getTitle(),
                entity.getCountry(),
                entity.getCity(),
                entity.getAddress(),
                entity.getLongitude(),
                entity.getLatitude(),
                entity.getDescription(),
                entity.getSqM(),
                entity.getTypeOfProperty(),
                entity.getTotal_Rooms(),
                entity.getTypeOfRental(),
                entity.getRentAmount(),
                entity.getSecurityDeposit(),
                entity.getIsAvailable(),
                entity.getIsActive(),
                entity.getOwnerId(),
                entity.getOwnerEthAddress(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

//    private RoomResponseDTO toDto(Room entity) {
//        if (entity == null) {
//            return null;
//        }
//
//        List<RoomImageResponseDTO> imageDtos = entity.getRoomImages().stream()
//                .map(this::toDto)
//                .collect(Collectors.toList());
//
//        return new RoomResponseDTO(
//                entity.getIdRoom(),
//                entity.getName(),
//                entity.getOrderIndex(),
//                imageDtos
//        );
//    }
//
//    private RoomImageResponseDTO toDto(RoomImage entity) {
//        if (entity == null) {
//            return null;
//        }
//        return new RoomImageResponseDTO(
//                entity.getIdImage(),
//                entity.getUrl(),
//                entity.getS3Key(),
//                entity.getOrderIndex()
//        );
//    }
}