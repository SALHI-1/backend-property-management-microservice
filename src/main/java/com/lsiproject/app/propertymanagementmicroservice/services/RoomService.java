package com.lsiproject.app.propertymanagementmicroservice.services;

import com.lsiproject.app.propertymanagementmicroservice.CreationDTOs.RoomCreationDTO;
import com.lsiproject.app.propertymanagementmicroservice.UpdateDTOs.RoomUpdateDTO;
import com.lsiproject.app.propertymanagementmicroservice.entities.Property;
import com.lsiproject.app.propertymanagementmicroservice.entities.Room;
import com.lsiproject.app.propertymanagementmicroservice.entities.RoomImage;
import com.lsiproject.app.propertymanagementmicroservice.repository.RoomImageRepository;
import com.lsiproject.app.propertymanagementmicroservice.repository.RoomRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Handles CRUD operations for Room entities.
 */
@Service
@AllArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final SupabaseStorageService storageService;
    private final RoomImageRepository imageRepository;

    /**
     * Creates a single Room entity, uploads its associated images to Supabase,
     * and persists the image metadata.
     * * @param property The parent Property entity (must be a persisted entity).
     * @param roomDto The DTO containing room name and image indices.
     * @return The newly created Room entity.
     */
    public Room createRoom(Property property, RoomCreationDTO roomDto) throws Exception {

        // Ensure parent is valid
        Long localPropertyId = property.getIdProperty();
        if (localPropertyId == null) {
            throw new IllegalArgumentException("Property must be saved before creating nested rooms.");
        }

        // 1. Create Room Entity
        Room room = new Room();
        room.setName(roomDto.name());
        room.setOrderIndex(roomDto.orderIndex());
        room.setProperty(property);


//        if (property.getRooms() == null) {
//            property.setRooms(new ArrayList<>());
//        }
//        property.getRooms().add(room);

//        property.setTotal_Rooms(property.getRooms().size());


        // 3. Save Room (This saves nested images due to cascade)
        return roomRepository.save(room);
    }

    /**
     * Updates descriptive fields of a room (name, orderIndex).
     * @param roomId The ID of the room to update.
     * @return The updated Room entity.
     */
    @Transactional
    public Room updateRoom(Long roomId, RoomUpdateDTO dto) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("Room not found with ID: " + roomId));

        if(dto.name() != null){room.setName(dto.name());}
        if(dto.orderIndex() != null){room.setOrderIndex(dto.orderIndex());}


        return roomRepository.save(room);
    }

    /**
     * Deletes a Room and automatically handles nested image file deletion on Supabase.
     * @param roomId The ID of the room to delete.
     */
    @Transactional
    public void deleteRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("Room not found with ID: " + roomId));

        // 1. Delete associated files from Supabase
        for (RoomImage image : room.getRoomImages()) {
            try {
                Long propertyId = room.getProperty().getIdProperty();
                String roomName = room.getName();
                String roomFolder = roomName.replaceAll("[^a-zA-Z0-9\\-_]", "-").toLowerCase();
                String objectPath = propertyId + "/" + roomFolder + "/" + image.getS3Key();

                //storageService.deleteFile(objectPath);

                // Remove from repository to prevent orphaned foreign keys, although cascade should handle this.
                imageRepository.delete(image);
            } catch (Exception e) {
                System.err.println("WARN: Failed to delete Supabase file for image ID " + image.getIdImage() + ": " + e.getMessage());
            }
        }

        // 2. Delete the Room entity (JPA cascade should delete RoomImage records)
        roomRepository.delete(room);
    }

    /**
     * Retrieves a single Room entity by its ID.
     * @param roomId The ID of the room to retrieve.
     * @return The Room entity.
     * @throws NoSuchElementException if the room is not found.
     */
    public Room getRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("Room not found with ID: " + roomId));
    }

    /**
     * Retrieves all Room entities.
     * @return A List of all Room entities.
     */
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    /**
     * Retrieves all rooms associated with a specific property ID.
     * @param propertyId The ID of the property.
     * @return List of Room entities.
     */
    public List<Room> getRoomsByPropertyId(Long propertyId) {
        // Utilisation de la méthode définie dans le repository
        return roomRepository.findByProperty_IdPropertyOrderByOrderIndexAsc(propertyId);
    }
}