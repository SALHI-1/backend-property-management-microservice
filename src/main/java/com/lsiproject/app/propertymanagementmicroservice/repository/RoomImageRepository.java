package com.lsiproject.app.propertymanagementmicroservice.repository;

import com.lsiproject.app.propertymanagementmicroservice.entities.RoomImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomImageRepository extends JpaRepository<RoomImage,Long> {
    List<RoomImage> findByRoom_IdRoom(Long room);
}
