# MicroService API Documentation

here u can find the way u can use all the APIs provided in this microservice


## Property API Documentation (Concise)
This microservice manages property listings, synchronizing data between the database (off-chain) and the blockchain (on-chain).

### Base Endpoint
All requests use the base path: /api/properties

### Authentication
For now All endpoints require a JWT Bearer Token in the Authorization header. User details (subject, Wallet Address, Roles) are extracted from this token.
but when the authentication and authorization microservices are completed , the jwt verification might be differant

1. Create Property (POST)
   /api/properties
   * Function: Creates a new property, saves it to the database, and performs a synchronous transaction to list it on the blockchain.

* Request: Send a JSON body of type PropertyCreationDTO(u can find it in src/java/main/CreationDTOs) (includes title, address, prices, etc.).

* Response Statuses:

201 Created: Returns the PropertyResponseDTO(u can find it in src/java/main/ResponseDTOs) for the new listing, including its onChainId.

500 Internal Server Error: Transaction or server failure.

2. Read Properties (GET)
   Get All Properties
   /api/properties
   Function: Retrieves a list of all active property listings from the database.


* Response Statuses:

* 200 OK: Returns a list of PropertyResponseDTO(u ca find it in src/java/main/ResponseDTOs).

3. Get Property by ID

/api/properties/{id}
* Function: Retrieves a single property using its local database ID.

* Path Variable: id (the local ID of the property).

* Response Statuses:

200 OK: Returns the matching PropertyResponseDTO.

404 Not Found: Property ID does not exist.

4. Update Property (PUT)
   /api/properties/{id}
   Function: Updates core property details (e.g., title, description, price, availability). This triggers a synchronous on-chain update.

* Access: Only the owner of the property.

* Path Variable: id (the local ID of the property).

* Request: Send a JSON body of type PropertyUpdateDTO(u ca find it in src/java/main/UpdateDTOs) with the fields to modify.

* Response Statuses:

200 OK: Returns the updated Property entity.

404 Not Found: Property ID does not exist.

403 Forbidden: Caller is not the property owner.

500 Internal Server Error: Transaction or server failure.

5. Delete Property (DELETE)
   /api/properties/{id}
   Function: Delists the property from the blockchain and marks it as inactive/unavailable in the database.

* Access: Only the owner of the property.

* Path Variable: id (the local ID of the property).

* Response Statuses:

204 No Content: Successful deletion.

404 Not Found: Property ID does not exist.

403 Forbidden: Caller is not the property owner.

500 Internal Server Error: Transaction or server failure.

## Room API Documentation (Concise)
This microservice handles the management of individual rooms and their associated images within a property listing.

* Base Endpoint
All requests use the base path: /api/rooms

* Authentication
All endpoints require a JWT Bearer Token in the Authorization header.

1. Create Room (POST) /api/rooms/property/{propertyId}

* Function: Creates a new room associated with a specific property. The logic for image upload/storage is handled in the service layer.

* Path Variable: propertyId (the local ID of the parent property).

* Request: Send a JSON body of type RoomCreationDTO (u can find it in src/java/main/CreationDTOs) (includes room name and order index).

* Response Statuses:

201 Created: Returns the RoomResponseDTO (u can find it in src/java/main/ResponseDTOs) for the new room.

400 Bad Request: Parent property ID does not exist (NoSuchElementException).

500 Internal Server Error: Server failure.

2. Read Rooms (GET) 2.1 Get All Rooms 
/api/rooms Function: Retrieves a list of all rooms across all properties from the database.

* Response Statuses:

200 OK: Returns a list of RoomResponseDTO (u ca find it in src/java/main/ResponseDTOs).

3. Get Room by ID 
    /api/rooms/{roomId} Function: Retrieves a single room using its local database ID.

* Path Variable: roomId (the local ID of the room).

* Response Statuses:

200 OK: Returns the matching RoomResponseDTO.

404 Not Found: Room ID does not exist.

4. Update Room (PUT) 
    /api/rooms/{roomId} Function: Updates the descriptive details of a room (name, order index).

* Path Variable: roomId (the local ID of the room).

* Request: Send a JSON body of type RoomUpdateDTO (u ca find it in src/java/main/UpdateDTOs) with the fields to modify.

* Response Statuses:

200 OK: Returns the updated RoomResponseDTO.

404 Not Found: Room ID does not exist.

5. Delete Room (DELETE)
   /api/rooms/{roomId} Function: Deletes a room and automatically handles the deletion of its associated image files from Supabase Storage.

* Path Variable: roomId (the local ID of the room).

* Response Statuses:

204 No Content: Successful deletion.

404 Not Found: Room ID does not exist.

## Room Image API Documentation (Concise)
This microservice handles the creation, retrieval, and deletion of image metadata and the physical files associated with a specific room.

Base Endpoint
All requests use the base path: /api/room-images

Authentication
All endpoints require a JWT Bearer Token in the Authorization header.

1. Create/Upload Image (POST) 
    /api/room-images/room/{roomId}

Function: Uploads a single image file to storage (Supabase) and creates its metadata record in the database, associating it with the specified room.

* Content Type: This endpoint requires multipart/form-data.

* Path Variable: roomId (the local ID of the parent room).

* Request: Multipart Form Data containing:

* imageFile: The actual image file.

* imageDto: A JSON object of type RoomImageCreationDTO (u can find it in src/java/main/CreationDTOs) (must include orderIndex).

* Response Statuses:

201 Created: Returns the RoomImageResponseDTO (u can find it in src/java/main/ResponseDTOs) containing the metadata and URL.

404 Not Found: Parent room ID does not exist.

500 Internal Server Error: File upload or server failure.

2. Read Room Images (GET) 
    Get Image by ID /api/room-images/{imageId} Function: Retrieves the metadata for a single image using its local database ID.

* Path Variable: imageId (the local ID of the image).

* Response Statuses:

200 OK: Returns the matching RoomImageResponseDTO.

404 Not Found: Image ID does not exist.

3. Get Images by Room ID 
    /api/room-images/room/{roomId} Function: Retrieves all image metadata records belonging to a specific room, usually ordered by orderIndex.

* Path Variable: roomId (the local ID of the parent room).

* Response Statuses:

200 OK: Returns a list of RoomImageResponseDTO.

404 Not Found: Room ID does not exist.

4. Update Image Order (PUT) 
    /api/room-images/{imageId}/order Function: Updates the orderIndex (position) of a specific image.

* Path Variable: imageId (the local ID of the image).

* Request: Send a JSON body of type RoomImageCreationDTO (u can find it in src/java/main/CreationDTOs) containing the new orderIndex.

* Response Statuses:

200 OK: Returns the updated RoomImageResponseDTO.

404 Not Found: Image ID does not exist.

5. Delete Image (DELETE) 
    /api/room-images/{imageId} Function: Deletes the image metadata from the database and the associated file from the storage service (Supabase).

* Path Variable: imageId (the local ID of the image).

* Response Statuses:

* 204 No Content: Successful deletion.

* 404 Not Found: Image ID does not exist.