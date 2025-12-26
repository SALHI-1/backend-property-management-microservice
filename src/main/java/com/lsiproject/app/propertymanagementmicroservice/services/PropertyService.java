package com.lsiproject.app.propertymanagementmicroservice.services;

import com.lsiproject.app.propertymanagementmicroservice.UpdateDTOs.PropertyUpdateDTO;
import com.lsiproject.app.propertymanagementmicroservice.contract.RealEstateRental;
import com.lsiproject.app.propertymanagementmicroservice.CreationDTOs.PropertyCreationDTO;
import com.lsiproject.app.propertymanagementmicroservice.entities.Property;
import com.lsiproject.app.propertymanagementmicroservice.repository.PropertyRepository;
import com.lsiproject.app.propertymanagementmicroservice.searchDTOs.PropertySearchDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.gas.StaticGasProvider;

import java.time.LocalDateTime;
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
     * Searches for properties based on dynamic criteria (City, Rent, Type, Location).
     * @param searchDTO Object containing filter parameters.
     * @return List of matching properties.
     */
    public List<Property> searchProperties(PropertySearchDTO searchDTO) {
        // Use the specification to build the query dynamically
        return propertyRepository.findAll(
                PropertySpecification.getPropertiesByCriteria(searchDTO)
        );
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


        Property property = new Property();
        property.setOnChainId(dto.onChainId());
        property.setTitle(dto.title());
        property.setCountry(dto.country());
        property.setCity(dto.city());
        property.setAddress(dto.address());
        property.setLongitude(dto.longitude());
        property.setLatitude(dto.latitude());
        property.setDescription(dto.description());
        property.setRentAmount(dto.rentAmount());
        property.setSecurityDeposit(dto.securityDeposit());
        property.setTypeOfRental(dto.typeOfRental());
        property.setOwnerId(ownerId);
        property.setOwnerEthAddress(ownerEthAddress);
        property.setIsActive(true);
        property.setIsAvailable(true);

        return propertyRepository.save(property);
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

        // 2. Update Off-Chain Data
        property.setTitle(dto.title()); //
        property.setCountry(dto.country());
        property.setCity(dto.city());
        property.setAddress(dto.address());
        property.setDescription(dto.description());
        property.setRentAmount(dto.rentAmount());
        property.setSecurityDeposit(dto.securityDeposit());
        property.setTypeOfRental(dto.typeOfRental());
        property.setIsAvailable(dto.isAvailable());

        property.setUpdatedAt(LocalDateTime.now());
        return propertyRepository.save(property);
    }

    public void updateAvailabilityToFalse(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        property.setIsAvailable(false);
        propertyRepository.save(property);
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

        // 2. Update Off-Chain Data
        property.setIsActive(false);
        property.setIsAvailable(false);
        property.setUpdatedAt(LocalDateTime.now());
        propertyRepository.save(property);
    }


    public List<Property> getAllProperties() {
        return propertyRepository.findAll();
    }

    public boolean isPropertyAvailable(Long id){
        return propertyRepository.existsByIdPropertyAndIsAvailableTrue(id);
    }

    /**
     * Récupère les 3 propriétés les plus récentes(just pour le moment, apres on vas utiliser un AI system pour ca) qui sont actives et disponibles.
     */
    public List<Property> getMostRecentProperties() {
        return propertyRepository.findTop3ByIsActiveTrueAndIsAvailableTrueOrderByCreatedAtDesc();
    }

    public List<Property> getPropertiesByOwnerId(Long ownerId) {
        return propertyRepository.findAllByOwnerId(ownerId);
    }

    /**
     * Retrieves a single property by ID and performs an on-chain status check
     * for verification. (This replaces the previous getPropertyById and getProperty).
     */
    public Property getProperty(Long id) throws Exception {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Property not found."));


        return property;
    }

}