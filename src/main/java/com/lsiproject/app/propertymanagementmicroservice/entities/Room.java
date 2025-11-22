package com.lsiproject.app.propertymanagementmicroservice.entities;

import com.lsiproject.app.propertymanagementmicroservice.entities.Property;
import com.lsiproject.app.propertymanagementmicroservice.entities.RoomImage;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Represents a specific room within a Property.
 * A Property can have multiple rooms (e.g., for multi-unit rentals).
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRoom;

    private String name;
    private int orderIndex;

    // --- Relationships ---

    // Many-to-One relationship back to the Property
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    // One-to-Many relationship with RoomImage
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomImage> roomImages;

}