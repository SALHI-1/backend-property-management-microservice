package com.lsiproject.app.propertymanagementmicroservice.entities;

import com.lsiproject.app.propertymanagementmicroservice.Enums.TypeOfRental;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.time.LocalDateTime;

/**
 * Represents a rental property managed by the microservice.
 * This entity is the source of truth for property details,
 * and its status must be synced with the RentalAgreement contract
 * (e.g., isAvailable in the contract corresponds to the property's rental status).
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "properties")
public class Property {

    //off-cjain id(in mysql)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProperty;

    //Link to the Smart Contract's property mapping ID
    @Column(unique = true, nullable = true) // Nullable until the first transaction confirms
    private Long onChainId;

    private String title;
    private String country;
    private String city;
    private String longitude;
    private String latitude;
    private String description;

    @Enumerated(EnumType.STRING)
    private TypeOfRental typeOfRental;

    private Long rentPerMonth;
    private Long securityDeposit;

    // --- Ownership and Status Fields ---

    // The Ethereum address of the owner/landlord. Crucial for contract interaction.
    private String ownerEthAddress;

    // The ID of the owner/landlord from the Auth Microservice.
    private Long ownerId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Boolean isActive; // Flag for soft deletion or listing status
    private Boolean isAvailable;
    private double rating;
    private int nombreEtoiles;

    // --- Relationships ---

    // One-to-Many relationship with Room
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Room> rooms;

}