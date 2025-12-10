package com.lsiproject.app.propertymanagementmicroservice.controllers;

import com.lsiproject.app.propertymanagementmicroservice.CreationDTOs.PropertyCreationDTO;
import com.lsiproject.app.propertymanagementmicroservice.ResponseDTOs.PropertyResponseDTO;
import com.lsiproject.app.propertymanagementmicroservice.UpdateDTOs.AvailabilityDTO;
import com.lsiproject.app.propertymanagementmicroservice.UpdateDTOs.PropertyUpdateDTO;
import com.lsiproject.app.propertymanagementmicroservice.mappers.PropertyMapper;
import com.lsiproject.app.propertymanagementmicroservice.entities.Property;
import com.lsiproject.app.propertymanagementmicroservice.searchDTOs.PropertySearchDTO;
import com.lsiproject.app.propertymanagementmicroservice.security.UserPrincipal;

import com.lsiproject.app.propertymanagementmicroservice.services.PropertyService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/property-microservice/properties")
@AllArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;
    private final PropertyMapper propertyMapper;

    // --- CREATE ---

    /**
     * Creates a new property listing, synchronizing data to the blockchain.
     * Accessible only by users with the LANDLORD role.
     * * @param dto The property creation data.
     * @param principal The authenticated user (extracted from JWT).
     * @return 201 Created with the new Property entity.
     */
    @PostMapping
    public ResponseEntity<PropertyResponseDTO> createProperty(
            @Valid @RequestBody PropertyCreationDTO dto,
            @AuthenticationPrincipal UserPrincipal principal) {

        try {
            Long ownerId = principal.getIdUser();
            String ownerEthAddress = principal.getWalletAddress();

            Property newProperty = propertyService.createProperty(dto,ownerId, ownerEthAddress);

            PropertyResponseDTO responseDto = propertyMapper.toDto(newProperty);

            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
        } catch (Exception e) {
            System.err.println("Property creation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    @PostMapping("/search")
    public ResponseEntity<List<Property>> searchProperties(@RequestBody PropertySearchDTO searchDTO) {
        List<Property> properties = propertyService.searchProperties(searchDTO);
        return ResponseEntity.ok(properties);
    }

    // --- READ ---
    /**
     * Retrieves all listed properties.
     * Accessible by any authenticated user.
     * * @return 200 OK with a list of properties.
     */
    @GetMapping
    public ResponseEntity<List<PropertyResponseDTO>> getAllProperties() {
        List<Property> property = propertyService.getAllProperties();

        List<PropertyResponseDTO> responseDto = new ArrayList<>();

        for(Property prop : property){
            PropertyResponseDTO newResponseDto = propertyMapper.toDto(prop);
            responseDto.add(newResponseDto);
        }

        System.out.println(responseDto);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * Retrieves a single property by its local database ID.
     * * @param id The local database ID of the property.
     * @return 200 OK with the Property entity.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponseDTO> getPropertyById(@PathVariable Long id) {
        try {
            Property property = propertyService.getProperty(id);

            PropertyResponseDTO responseDto = propertyMapper.toDto(property);

            return ResponseEntity.ok(responseDto);

        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();

        } catch (Exception e) {

            System.err.println("Property read failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/isAvailable")
    public ResponseEntity<Boolean> isPropertyAvailable(@PathVariable Long id) {
        return ResponseEntity.ok(propertyService.isPropertyAvailable(id));
    }



    // --- UPDATE ---

    /**
     * Updates the infos of an existing property, synchronizing changes to the blockchain.
     * it doesn't include the rooms and the images , only the infos like(description , address ....)
     * Accessible only by the owner of the property (implicit check in the service layer).
     * * @param id The local database ID of the property to update.
     * @param principal The authenticated user (for authorization check).
     * @return 200 OK with the updated Property entity.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Property> updateProperty(
            @PathVariable Long id,
            @Valid @RequestBody PropertyUpdateDTO updatedProperty,
            @AuthenticationPrincipal UserPrincipal principal) {

        try {
            String currentOwnerEthAddress = principal.getWalletAddress();

            Property result = propertyService.updateProperty(id, updatedProperty, currentOwnerEthAddress);
            return ResponseEntity.ok(result);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            System.err.println("Property update failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<Void> updateAvailabilityToFalse(
            @PathVariable Long id) {

        propertyService.updateAvailabilityToFalse(id);
        return ResponseEntity.noContent().build();
    }


    // --- DELETE ---

    /**
     * Delists a property on-chain and marks it inactive in the database.
     * * @param id The local database ID of the property to delete.
     * @param principal The authenticated user.
     * @return 204 No Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {

        try {
            String currentOwnerEthAddress = principal.getWalletAddress();
            propertyService.deleteProperty(id, currentOwnerEthAddress);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            System.err.println("Property deletion failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}