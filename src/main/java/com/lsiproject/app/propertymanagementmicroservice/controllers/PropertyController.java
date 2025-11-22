package com.lsiproject.app.propertymanagementmicroservice.controllers;

import com.lsiproject.app.propertymanagementmicroservice.DTOs.PropertyCreationDTO;
import com.lsiproject.app.propertymanagementmicroservice.DTOs.PropertyUpdateDTO;
import com.lsiproject.app.propertymanagementmicroservice.entities.Property;
import com.lsiproject.app.propertymanagementmicroservice.security.UserPrincipal;

import com.lsiproject.app.propertymanagementmicroservice.services.PropertyService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/api/properties")
@AllArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    // --- CREATE ---

    /**
     * Creates a new property listing, synchronizing data to the blockchain.
     * Accessible only by users with the LANDLORD role.
     * * @param dto The property creation data.
     * @param principal The authenticated user (extracted from JWT).
     * @return 201 Created with the new Property entity.
     */
    @PostMapping
    public ResponseEntity<Property> createProperty(
            @Valid @RequestBody PropertyCreationDTO dto,
            @AuthenticationPrincipal UserPrincipal principal) {

        try {
            // Extract necessary data from the Security Context (Stateless Auth)
            Long ownerId = principal.getIdUser();
            String ownerEthAddress = principal.getWalletAddress();

            Property newProperty = propertyService.createProperty(dto, ownerId, ownerEthAddress);
            return new ResponseEntity<>(newProperty, HttpStatus.CREATED);
        } catch (Exception e) {
            // Log the exception (e.g., blockchain transaction failure)
            System.err.println("Property creation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    // --- READ ---

    /**
     * Retrieves all listed properties.
     * Accessible by any authenticated user.
     * * @return 200 OK with a list of properties.
     */
    @GetMapping
    public ResponseEntity<List<Property>> getAllProperties() {
        return ResponseEntity.ok(propertyService.getAllProperties());
    }

    /**
     * Retrieves a single property by its local database ID.
     * * @param id The local database ID of the property.
     * @return 200 OK with the Property entity.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Property> getPropertyById(@PathVariable Long id) {
        try {
            Property property = propertyService.getProperty(id);
            return ResponseEntity.ok(property);

        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();

        } catch (Exception e) {

            System.err.println("Property read failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    // --- UPDATE ---

    /**
     * Updates an existing property, synchronizing changes to the blockchain.
     * Accessible only by the owner of the property (implicit check in the service layer).
     * * @param id The local database ID of the property to update.
     * @param updatedProperty The new property data.
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