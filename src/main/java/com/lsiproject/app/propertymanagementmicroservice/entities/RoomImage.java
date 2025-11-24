package com.lsiproject.app.propertymanagementmicroservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Stores the metadata for images associated with a Room.
 * The actual image files should be stored in a cloud service (e.g., AWS S3).
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "room_images")
public class RoomImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idImage;

    private String url;
    private String s3Key;
    private int orderIndex;
    private LocalDateTime uploadedAt;

    // --- Relationships ---

    // Many-to-One relationship back to the Room
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

}