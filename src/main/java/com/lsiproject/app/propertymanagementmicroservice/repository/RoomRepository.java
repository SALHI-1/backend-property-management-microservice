package com.lsiproject.app.propertymanagementmicroservice.repository;

import com.lsiproject.app.propertymanagementmicroservice.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room,Long> {
    List<Room> findByProperty_IdPropertyOrderByOrderIndexAsc(Long propertyId);
}
