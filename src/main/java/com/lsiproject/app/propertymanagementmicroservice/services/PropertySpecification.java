package com.lsiproject.app.propertymanagementmicroservice.services;

import com.lsiproject.app.propertymanagementmicroservice.entities.Property;
import com.lsiproject.app.propertymanagementmicroservice.searchDTOs.PropertySearchDTO;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PropertySpecification {

    public static Specification<Property> getPropertiesByCriteria(PropertySearchDTO criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // --- Standard Filters ---

            // 1. City (Case insensitive)
            if (criteria.getCity() != null && !criteria.getCity().isEmpty()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("city")),
                        criteria.getCity().toLowerCase()
                ));
            }

            // 2. Rent Amount Range
            if (criteria.getMinRentAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rentAmount"), criteria.getMinRentAmount()));
            }
            if (criteria.getMaxRentAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("rentAmount"), criteria.getMaxRentAmount()));
            }

            // 3. Rental Type
            if (criteria.getTypeOfRental() != null) {
                predicates.add(cb.equal(root.get("typeOfRental"), criteria.getTypeOfRental()));
            }

            // 4. Status Checks (Must be active/available)
            predicates.add(cb.isTrue(root.get("isActive")));
            predicates.add(cb.isTrue(root.get("isAvailable")));

            // --- GEOLOCATION FILTER (Haversine Formula) ---

            if (criteria.getLatitude() != null && criteria.getLongitude() != null) {
                double earthRadius = 6371.0; // Radius of Earth in KM
                double searchRadius = (criteria.getRadiusInKm() != null) ? criteria.getRadiusInKm() : 5.0;

                // Inputs from the user (converted to radians for the formula)
                double userLatRad = Math.toRadians(criteria.getLatitude());
                double userLonRad = Math.toRadians(criteria.getLongitude());

                // Database columns (converted to radians)
                // syntax: cb.function("function_name", ReturnType.class, Arguments...)
                Expression<Double> propLatRad = cb.function("radians", Double.class, root.get("latitude"));
                Expression<Double> propLonRad = cb.function("radians", Double.class, root.get("longitude"));

                // The Haversine Formula:
                // d = acos( sin(lat1)*sin(lat2) + cos(lat1)*cos(lat2)*cos(lon2-lon1) ) * R

                // Part 1: sin(userLat) * sin(propLat)
                Expression<Double> term1 = cb.prod(cb.literal(Math.sin(userLatRad)), cb.function("sin", Double.class, propLatRad));

                // Part 2: cos(userLat) * cos(propLat)
                Expression<Double> term2A = cb.prod(cb.literal(Math.cos(userLatRad)), cb.function("cos", Double.class, propLatRad));

                // Part 3: cos(propLon - userLon)
                Expression<Double> lonDiff = cb.diff(propLonRad, cb.literal(userLonRad));
                Expression<Double> term2B = cb.function("cos", Double.class, lonDiff);

                // Combine terms: term1 + (term2A * term2B)
                Expression<Double> formulaInsideAcos = cb.sum(term1, cb.prod(term2A, term2B));

                // Calculate Distance: earthRadius * acos( formula )
                Expression<Double> distance = cb.prod(cb.literal(earthRadius), cb.function("acos", Double.class, formulaInsideAcos));

                // Final Predicate: distance < searchRadius
                predicates.add(cb.lessThan(distance, searchRadius));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}