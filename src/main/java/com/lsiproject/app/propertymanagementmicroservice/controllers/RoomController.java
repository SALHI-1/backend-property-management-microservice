package com.lsiproject.app.propertymanagementmicroservice.controllers;

import com.lsiproject.app.propertymanagementmicroservice.CreationDTOs.RoomCreationDTO;
import com.lsiproject.app.propertymanagementmicroservice.ResponseDTOs.RoomResponseDTO;
import com.lsiproject.app.propertymanagementmicroservice.UpdateDTOs.RoomUpdateDTO;
import com.lsiproject.app.propertymanagementmicroservice.entities.Property;
import com.lsiproject.app.propertymanagementmicroservice.entities.Room;
import com.lsiproject.app.propertymanagementmicroservice.mappers.RoomMapper;
import com.lsiproject.app.propertymanagementmicroservice.services.PropertyService; // Assuming you have a PropertyService to fetch the parent Property
import com.lsiproject.app.propertymanagementmicroservice.services.RoomService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rooms")
@AllArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final RoomMapper roomMapper;
    private final PropertyService propertyService;

    /**
     * GET /api/rooms : Retrieve a list of all rooms.
     */
    @GetMapping
    public ResponseEntity<List<RoomResponseDTO>> getAllRooms() {
        List<Room> rooms = roomService.getAllRooms();

        List<RoomResponseDTO> roomsDTO = rooms.stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(roomsDTO);
    }

    /**
     * GET /api/rooms/{roomId} : Retrieve a room by its ID.
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponseDTO> getRoomById(@PathVariable Long roomId) {
        try {
            Room room = roomService.getRoomById(roomId);

            RoomResponseDTO roomDTO = roomMapper.toDto(room);

            return ResponseEntity.ok(roomDTO);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/rooms/property/{propertyId} : Create a new room with associated images.
     * Consumes multipart/form-data.
     */
    @PostMapping("/property/{propertyId}")
    public ResponseEntity<RoomResponseDTO> createRoom(
            @PathVariable Long propertyId,
            @RequestBody @Valid RoomCreationDTO roomDto){
        try {

            Property property = propertyService.getProperty(propertyId);

            Room createdRoom = roomService.createRoom(property, roomDto);

            return ResponseEntity.status(HttpStatus.CREATED).body(roomMapper.toDto(createdRoom));

        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body(null); // Parent property not found
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * PUT /api/rooms/{roomId} : Update the name and order index of a room.
     */
    @PutMapping("/{roomId}")
    public ResponseEntity<RoomResponseDTO> updateRoom(
            @PathVariable Long roomId,
            @RequestBody RoomUpdateDTO dto) {
        try {
            Room updatedRoom = roomService.updateRoom(roomId, dto);

            return ResponseEntity.ok(roomMapper.toDto(updatedRoom));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/rooms/{roomId} : Delete a room and its associated images.
     */
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        try {
            roomService.deleteRoom(roomId);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}