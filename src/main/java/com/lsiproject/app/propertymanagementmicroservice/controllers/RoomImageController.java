package com.lsiproject.app.propertymanagementmicroservice.controllers;

import com.lsiproject.app.propertymanagementmicroservice.CreationDTOs.RoomImageCreationDTO;
import com.lsiproject.app.propertymanagementmicroservice.ResponseDTOs.RoomImageResponseDTO;
import com.lsiproject.app.propertymanagementmicroservice.entities.Room;
import com.lsiproject.app.propertymanagementmicroservice.entities.RoomImage;
import com.lsiproject.app.propertymanagementmicroservice.mappers.RoomImageMapper;
import com.lsiproject.app.propertymanagementmicroservice.services.RoomImageService;
import com.lsiproject.app.propertymanagementmicroservice.services.RoomService; // Needed to fetch parent Room
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/property-microservice/properties/room-images")
@AllArgsConstructor
public class RoomImageController {

    private final RoomImageService imageService;
    private final RoomService roomService; // Dependency needed to fetch parent room
    private final RoomImageMapper imageMapper;

    /**
     * GET /api/room-images/{imageId} : Get image metadata by ID.
     * NOTE: Requires imageService.getImageById(imageId) to exist.
     */

    @GetMapping("/{imageId}")
    public ResponseEntity<RoomImageResponseDTO> getImageById(@PathVariable Long imageId) {
        try {

            RoomImage image = imageService.getImageById(imageId);
            return ResponseEntity.ok(imageMapper.toDto(image));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/room-images/room/{roomId} : Get all images belonging to a specific room.
     * NOTE: Requires imageService.getImagesByRoomId(roomId) to exist.
     */
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<RoomImageResponseDTO>> getImagesByRoomId(@PathVariable Long roomId) {
        try {
            // Assuming imageService has a retrieval method
            List<RoomImage> images = imageService.getImagesByRoomId(roomId);

            List<RoomImageResponseDTO> dtoImages= new ArrayList<>();

            for(RoomImage image : images) {
                RoomImageResponseDTO dtoImage = imageMapper.toDto(image);
                dtoImages.add(dtoImage);
            }
            return ResponseEntity.ok(dtoImages);
        } catch (NoSuchElementException e) {
            // If the room doesn't exist, we return 404 (or 200 with an empty list if query handles it gracefully)
            return ResponseEntity.notFound().build();
        }
    }


    /**
     * POST /api/room-images/room/{roomId} : Upload a new image and create metadata.
     * Consumes multipart/form-data.
     */
    @PostMapping(
            path = "/room/{roomId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<RoomImageResponseDTO> uploadImage(
            @PathVariable Long roomId,
            @RequestPart("imageFile") MultipartFile imageFile,
            @RequestPart("imageDto") @Valid RoomImageCreationDTO imageDto) {
        try {
            // 1. Fetch the parent Room entity
            Room room = roomService.getRoomById(roomId);

            // 2. Create the image and upload the file
            RoomImage createdImage = imageService.createImage(room, imageFile, imageDto);

            // 3. Return the DTO response
            return ResponseEntity.status(HttpStatus.CREATED).body(imageMapper.toDto(createdImage));

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Parent room not found
        } catch (Exception e) {
            System.err.println("Image upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PUT /api/room-images/{imageId}/order : Update the order index of a specific image.
     */
    @PutMapping("/{imageId}/order")
    public ResponseEntity<RoomImageResponseDTO> updateImageOrder(
            @PathVariable Long imageId,
            @RequestBody @Valid RoomImageCreationDTO imageDto) {
        try {
            RoomImage updatedImage = imageService.updateImageOrder(imageId, imageDto);
            return ResponseEntity.ok(imageMapper.toDto(updatedImage));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/room-images/{imageId} : Delete image metadata and the file from storage.
     */
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long imageId) {
        try {
            imageService.deleteImage(imageId);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            // Return 404 if the image was not found
            return ResponseEntity.notFound().build();
        }
    }
}