package com.lsiproject.app.propertymanagementmicroservice.services;

import com.lsiproject.app.propertymanagementmicroservice.CreationDTOs.RoomImageCreationDTO;
import com.lsiproject.app.propertymanagementmicroservice.entities.Room;
import com.lsiproject.app.propertymanagementmicroservice.entities.RoomImage;
import com.lsiproject.app.propertymanagementmicroservice.repository.RoomImageRepository;
import com.lsiproject.app.propertymanagementmicroservice.repository.RoomRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Handles CRUD operations for RoomImage entities, including file synchronization with Supabase.
 */
@Service
@AllArgsConstructor
public class RoomImageService {

    private final RoomImageRepository imageRepository;
    private final RoomRepository roomRepository;
    private final SupabaseStorageService storageService;

    /**
     * Creates a new image metadata record and uploads the file to Supabase.
     * This method is designed to be called by RoomService during batch room creation.
     * @param room The parent Room entity (must be persisted).
     * @param file The actual image file.
     * @return The newly created RoomImage entity.
     */
    @Transactional
    public RoomImage createImage(Room room, MultipartFile file, RoomImageCreationDTO roomDTO) throws Exception {

        // Get info needed for Supabase pathing
        Long propertyId = room.getProperty().getIdProperty();
        String roomName = room.getName();
        String roomFolder = roomName.replaceAll("[^a-zA-Z0-9\\-_]", "-").toLowerCase();

        // 1. Upload to Supabase
        String imageUrl = storageService.uploadImageFile(file, propertyId, roomFolder);
        String s3Key = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);

        // 2. Save Metadata
        RoomImage image = new RoomImage();
        image.setUrl(imageUrl);
        image.setS3Key(s3Key);
        image.setOrderIndex(roomDTO.orderIndex());
        image.setUploadedAt(LocalDateTime.now());
        image.setRoom(room);

        return imageRepository.save(image);
    }

    /**
     * Updates the order index of a specific image.
     * @param imageId The ID of the image to update.
     * @return The updated RoomImage.
     */
    @Transactional
    public RoomImage updateImageOrder(Long imageId, RoomImageCreationDTO roomDTO) {
        RoomImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new NoSuchElementException("Room image not found with ID: " + imageId));

        image.setOrderIndex(roomDTO.orderIndex());
        return imageRepository.save(image);
    }

    /**
     * Deletes image metadata from the database and the file from Supabase.
     * @param imageId The ID of the image to delete.
     */
    @Transactional
    public void deleteImage(Long imageId) {
        RoomImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new NoSuchElementException("Room image not found with ID: " + imageId));

        // 1. Delete file from Supabase
        try {
            // Path structure is bucketName/propertyId/roomName/s3Key
            Long propertyId = image.getRoom().getProperty().getIdProperty();
            String roomName = image.getRoom().getName();
            String roomFolder = roomName.replaceAll("[^a-zA-Z0-9\\-_]", "-").toLowerCase();
            String objectPath = propertyId + "/" + roomFolder + "/" + image.getS3Key();

            storageService.deleteFile(objectPath);
        } catch (Exception e) {
            System.err.println("WARN: Failed to delete file from Supabase: " + image.getUrl() + ". Proceeding with DB deletion.");
        }

        imageRepository.delete(image);
    }

    /**
     * Retrieves a single RoomImage entity by its ID.
     * @param imageId The ID of the image to retrieve.
     * @return The RoomImage entity.
     * @throws NoSuchElementException if the image is not found.
     */
    public RoomImage getImageById(Long imageId) {
        return imageRepository.findById(imageId)
                .orElseThrow(() -> new NoSuchElementException("Room image not found with ID: " + imageId));
    }

    /**
     * Retrieves all RoomImage entities associated with a specific Room ID.
     * @param roomId The ID of the parent room.
     * @return A List of RoomImage entities.
     * @throws NoSuchElementException if the parent room is not found.
     */
    public List<RoomImage> getImagesByRoomId(Long roomId) {
        // Ensure the parent room exists before querying for images
        if (!roomRepository.existsById(roomId)) {
            throw new NoSuchElementException("Room not found with ID: " + roomId + ". Cannot retrieve images.");
        }

        // Assumes RoomImageRepository has a method like findByRoom_IdRoom(Long roomId)
        // derived from the Room entity's primary key (idRoom) field.
        return imageRepository.findByRoom_IdRoom(roomId);
    }
}