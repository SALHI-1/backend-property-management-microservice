package com.lsiproject.app.propertymanagementmicroservice.repository;

import com.lsiproject.app.propertymanagementmicroservice.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room,Integer> {
}
