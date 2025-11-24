package com.lsiproject.app.propertymanagementmicroservice.services;

import com.lsiproject.app.propertymanagementmicroservice.UpdateDTOs.PropertyUpdateDTO;
import com.lsiproject.app.propertymanagementmicroservice.CreationDTOs.RoomCreationDTO;
import com.lsiproject.app.propertymanagementmicroservice.Enums.TypeOfRental;
import com.lsiproject.app.propertymanagementmicroservice.contract.RealEstateRental;
import com.lsiproject.app.propertymanagementmicroservice.CreationDTOs.PropertyCreationDTO;
import com.lsiproject.app.propertymanagementmicroservice.entities.Property;
import com.lsiproject.app.propertymanagementmicroservice.entities.Room;
import com.lsiproject.app.propertymanagementmicroservice.entities.RoomImage;
import com.lsiproject.app.propertymanagementmicroservice.repository.PropertyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Service to manage property CRUD operations, synchronizing off-chain data (MySQL)
 * with on-chain data (RealEstateRental contract).
 */
@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final RealEstateRental rentalContract;
    private final SupabaseStorageService storageService;
    private final RoomService roomService;

    public PropertyService(
            PropertyRepository propertyRepository,
            Web3j web3j,
            Credentials credentials,
            StaticGasProvider gasProvider,
            RoomService roomService,
            @Value("${contract.rental.address}") String contractAddress,
            SupabaseStorageService storageService
    ) {
        this.propertyRepository = propertyRepository;
        this.roomService = roomService;

        // Load the deployed contract wrapper
        this.rentalContract = RealEstateRental.load(
                contractAddress, web3j, credentials, gasProvider
        );
        this.storageService = storageService;
    }

    /**
     * Creates a new property off-chain, lists it on-chain, and uploads rooms/images to Supabase.
     * @param dto The property details, including nested rooms/images.
     * @param ownerId The ID of the owner (from JWT claims).
     * @param ownerEthAddress The wallet address of the owner (from JWT claims).
     * @return The saved Property entity.
     * @throws Exception if the blockchain transaction fails.
     */
    @Transactional
    public Property createProperty(
            PropertyCreationDTO dto,
            Long ownerId,
            String ownerEthAddress
    ) throws Exception {

        // 1. Prepare Off-Chain Entity (basic fields)
        Property property = new Property();
        property.setTitle(dto.title());
        property.setCountry(dto.country());
        property.setCity(dto.city());
        property.setAddress(dto.address());
        property.setDescription(dto.description());
        property.setRentPerMonth(dto.rentPerMonth());
        property.setSecurityDeposit(dto.securityDeposit());
        property.setTypeOfRental(dto.typeOfRental());
        property.setOwnerId(ownerId);
        property.setOwnerEthAddress(ownerEthAddress);
        property.setIsActive(true);
        property.setIsAvailable(true);

        // Save preliminary to get ID
        Property savedProperty = propertyRepository.save(property);
        Long localPropertyId = savedProperty.getIdProperty();

        // 2. Blockchain Transaction
        var receipt = rentalContract.listProperty(
                dto.fullAddress(),
                dto.description(),
                BigInteger.valueOf(dto.rentPerMonth()),
                BigInteger.valueOf(dto.securityDeposit())
        ).send();

        var events = rentalContract.getPropertyListedEvents(receipt);
        if (events.isEmpty()) {
            propertyRepository.delete(savedProperty);
            throw new RuntimeException("Blockchain event missing - rollback");
        }

        Long onChainId = events.get(0).propertyId.longValue();
        savedProperty.setOnChainId(onChainId);

        return propertyRepository.save(savedProperty);
    }


    /**
     * Updates a property off-chain and synchronously updates the details on-chain.
     * @param id The database ID (idProperty).
     * @param dto The property entity with new data.
     * @param currentOwnerEthAddress The wallet address of the caller (for on-chain authorization).
     * @return The updated Property entity.
     * @throws Exception if the blockchain transaction fails or property not found.
     */
    @Transactional
    public Property updateProperty(Long id, PropertyUpdateDTO dto, String currentOwnerEthAddress) throws Exception {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Property not found in database."));

        // Ensure the caller is authorized (off-chain check)
        if (!property.getOwnerEthAddress().equalsIgnoreCase(currentOwnerEthAddress)) {
            throw new SecurityException("Caller is not the property owner.");
        }

        // Ensure onChainId is present before updating
        Long onChainId = property.getOnChainId();
        if (onChainId == null) {
            throw new IllegalStateException("Property is not yet listed on chain.");
        }

        // 1. Send Update Transaction to Blockchain (using DTO fields)
        rentalContract.updateProperty(
                BigInteger.valueOf(onChainId),
                dto.fullAddress(),
                dto.description(),
                BigInteger.valueOf(dto.rentPerMonth()),
                BigInteger.valueOf(dto.securityDeposit()),
                dto.isAvailable()
        ).send();

        // 2. Update Off-Chain Data
        property.setTitle(dto.title()); //
        property.setCountry(dto.country());
        property.setCity(dto.city());
        property.setAddress(dto.address());
        property.setDescription(dto.description());
        property.setRentPerMonth(dto.rentPerMonth());
        property.setSecurityDeposit(dto.securityDeposit());
        property.setTypeOfRental(dto.typeOfRental());
        property.setIsAvailable(dto.isAvailable());

        property.setUpdatedAt(LocalDateTime.now());
        return propertyRepository.save(property);
    }

    /**
     * Deletes a property by delisting it on-chain and marking it inactive in the database.
     * @param id The database ID (idProperty).
     * @param currentOwnerEthAddress The wallet address of the caller.
     * @throws Exception if the blockchain transaction fails or property not found.
     */
    @Transactional
    public void deleteProperty(Long id, String currentOwnerEthAddress) throws Exception {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Property not found in database."));

        // Ensure the caller is authorized (off-chain check)
        if (!property.getOwnerEthAddress().equalsIgnoreCase(currentOwnerEthAddress)) {
            throw new SecurityException("Caller is not the property owner.");
        }

        Long onChainId = property.getOnChainId();
        if (onChainId == null) {
            // If not on chain, just delete it locally
            propertyRepository.delete(property);
            return;
        }

        // 1. Send Delist Transaction to Blockchain
        rentalContract.delistProperty(BigInteger.valueOf(onChainId)).send();

        // 2. Update Off-Chain Data
        property.setIsActive(false);
        property.setIsAvailable(false);
        property.setUpdatedAt(LocalDateTime.now());
        propertyRepository.save(property);
    }


    public List<Property> getAllProperties() {
        return propertyRepository.findAll();
    }


    /**
     * TEMPORARY FUNCTION FOR TESTING WEB3J CONNECTIVITY.
     * Fetches ALL existing properties directly from the blockchain by iterating the propertyCounter.
     * NOTE: This is slow and should NOT be used in production.
     * @return List of Property objects mapped from the blockchain data.
     */
    public List<Property> getAllPropertiesFromChain() throws Exception {
        List<Property> properties = new ArrayList<>();

        // 1. Get total number of properties listed on the contract
        BigInteger counter = rentalContract.propertyCounter().send();
        long totalProperties = counter.longValue();

        // 2. Loop from ID 1 up to the total count
        for (long i = 1; i <= totalProperties; i++) {
            try {
                // 3. Call the view function to retrieve the Solidity struct data
                RealEstateRental.Property onChainData = rentalContract.getProperty(
                        BigInteger.valueOf(i)
                ).send();

                // 4. Map the blockchain data (minimal) to the Java Entity (detailed)
                Property p = new Property();
                p.setOnChainId(i);
                p.setOwnerEthAddress(onChainData.owner);

                // Note: The Property Entity requires more fields than the contract provides (title, city, etc.).
                // We will populate them with placeholder/minimal values for testing.
                p.setTitle("Property ID " + i + " (On-Chain)");
                p.setDescription(onChainData.description);
                p.setRentPerMonth(onChainData.rentPerMonth.longValue());
                p.setSecurityDeposit(onChainData.securityDeposit.longValue());
                p.setIsAvailable(onChainData.isAvailable);
                p.setIsActive(onChainData.isActive);
                p.setTypeOfRental(TypeOfRental.MONTHLY); // Default for test view

                properties.add(p);
            } catch (Exception e) {
                // If getProperty(i) fails (e.g., if a property was deleted without cleaning up the counter), skip it.
                System.err.println("WARN: Failed to retrieve property ID " + i + " from blockchain: " + e.getMessage());
            }
        }

        return properties;
    }

    /**
     * Retrieves a single property by ID and performs an on-chain status check
     * for verification. (This replaces the previous getPropertyById and getProperty).
     */
    public Property getProperty(Long id) throws Exception {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Property not found."));

        // this just for testing, it will not be used in production
        //verifyOnChainStatus(property);

        return property;
    }

    /**
     * Helper method to verify the local property status against the blockchain record.
     * This is a non-essential check but provides strong data integrity.
     */
    private void verifyOnChainStatus(Property property) {
        if (property.getOnChainId() != null) {
            try {
                // Read the contract's copy of the property struct
                // NOTE: The Web3j generated wrapper will return a complex type
                // containing all struct fields (id, owner, propertyAddress, description, rentPerMonth, securityDeposit, isAvailable, isActive).
                RealEstateRental.Property onChainData = rentalContract.getProperty(
                        BigInteger.valueOf(property.getOnChainId())
                ).send();

                // Example sync check 1: Availability status
                if (property.getIsAvailable() != onChainData.isAvailable) {
                    System.err.println("WARN: Property ID " + property.getIdProperty() + " Off-chain availability mismatch with blockchain.");
                    // In a real application, you might update the database here.
                }

                // Example sync check 2: Active status
                if (property.getIsActive() != onChainData.isActive) {
                    System.err.println("WARN: Property ID " + property.getIdProperty() + " Off-chain active status mismatch with blockchain.");
                }
            } catch (Exception e) {
                // Do not throw an exception here, as a failed read (e.g., node down)
                // should not prevent the user from seeing the off-chain data.
                System.err.println("ERROR: Could not verify property status on-chain for ID " + property.getIdProperty() + ": " + e.getMessage());
            }
        }
    }
}