package com.lsiproject.app.propertymanagementmicroservice.searchDTOs;

import com.lsiproject.app.propertymanagementmicroservice.Enums.TypeOfRental;
import lombok.Data;

@Data
public class PropertySearchDTO {
    private String city;
    private Long minRentAmount;
    private Long maxRentAmount;
    private TypeOfRental typeOfRental;
    private Double latitude;
    private Double longitude;

    // Default radius to 5.0 km if not provided, or let frontend decide
    private Double radiusInKm = 5.0;
}