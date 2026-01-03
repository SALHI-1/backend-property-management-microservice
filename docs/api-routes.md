# Property Management Microservice — Documentation des Routes et DTOs

Ce document recense les endpoints REST exposés par le microservice de gestion des propriétés, ainsi que les DTOs pour les requêtes et réponses. Les chemins et les payloads sont dérivés du code des contrôleurs et DTOs présents dans le projet.

Base path commun:
- Propriétés: `/api/property-microservice/properties`
- Chambres: `/api/property-microservice/rooms`
- Images de chambre: `/api/property-microservice/properties/room-images`

Notes d’authentification et rôles:
- Certaines routes exigent un utilisateur authentifié, avec récupération du principal (`UserPrincipal`).
- La création et la suppression de propriété impliquent l’adresse Ethereum et l’ID propriétaire du principal. La logique d’autorisation (propriétaire/landlord) est gérée dans le service.

---

## Propriétés — `/api/property-microservice/properties`

### POST `/api/property-microservice/properties`
Crée une nouvelle propriété (listing) et synchronise sur la blockchain.

- Auth: Requiert un utilisateur authentifié (landlord). Utilise `principal.getIdUser()` et `principal.getWalletAddress()`.
- Request Body: `PropertyCreationDTO`
  - `onChainId` (Long, required)
  - `title` (String, required)
  - `country` (String, required)
  - `city` (String, required)
  - `address` (String, required)
  - `longitude` (Double, required)
  - `latitude` (Double, required)
  - `description` (String, required)
  - `sqM` (Integer, required)
  - `typeOfProperty` (Enum PropertyType, required)
  - `typeOfRental` (Enum TypeOfRental, required)
  - `rentAmount` (Long, required)
  - `securityDeposit` (Long, required)
- Response: `201 Created` avec `PropertyResponseDTO`

`PropertyResponseDTO`:
- `idProperty` (Long)
- `onChainId` (Long)
- `title` (String)
- `country` (String)
- `city` (String)
- `address` (String)
- `longitude` (Double)
- `latitude` (Double)
- `description` (String)
- `sqm` (Integer)
- `typeOfProperty` (Enum PropertyType)
- `totalRooms` (Integer)
- `typeOfRental` (Enum TypeOfRental)
- `rentAmount` (Long)
- `securityDeposit` (Long)
- `isAvailable` (Boolean)
- `isActive` (Boolean)
- `ownerId` (Long)
- `ownerEthAddress` (String)
- `createdAt` (LocalDateTime)
- `updatedAt` (LocalDateTime)

---

### POST `/api/property-microservice/properties/search`
Recherche de propriétés avec critères.

- Auth: Non spécifiée (probablement public ou utilisateur authentifié).
- Request Body: `PropertySearchDTO`
  - `city` (String, optional)
  - `minRentAmount` (Long, optional)
  - `maxRentAmount` (Long, optional)
  - `typeOfRental` (Enum TypeOfRental, optional)
  - `latitude` (Double, optional)
  - `longitude` (Double, optional)
  - `radiusInKm` (Double, optional, défaut 5.0)
- Response: `200 OK` avec `List<PropertyResponseDTO>`

---

### GET `/api/property-microservice/properties`
Récupère la liste de toutes les propriétés.

- Auth: Non spécifiée (souvent public ou utilisateur authentifié).
- Response: `200 OK` avec `List<PropertyResponseDTO>`

---

### GET `/api/property-microservice/properties/{id}`
Récupère une propriété par son ID (base locale).

- Path Params:
  - `id` (Long)
- Response:
  - `200 OK` avec `PropertyResponseDTO`
  - `404 Not Found` si introuvable
  - `500 Internal Server Error` en cas d’erreur serveur

---

### GET `/api/property-microservice/properties/{id}/isAvailable`
Retourne la disponibilité de la propriété.

- Path Params:
  - `id` (Long)
- Response: `200 OK` avec `Boolean`

---

### GET `/api/property-microservice/properties/featured`
Récupère jusqu’à 3 propriétés récentes actives et disponibles.

- Auth: Public
- Response: `200 OK` avec `List<PropertyResponseDTO>`

---

### GET `/api/property-microservice/properties/my-properties`
Récupère toutes les propriétés appartenant à l’utilisateur authentifié.

- Auth: Requiert un utilisateur authentifié (`UserPrincipal`).
- Response: `200 OK` avec `List<PropertyResponseDTO>`

---

### PUT `/api/property-microservice/properties/{id}`
Met à jour les informations d’une propriété (hors rooms/images) et synchronise les changements on-chain.

- Auth: Requiert l’adresse Ethereum du propriétaire (`principal.getWalletAddress()`); vérifications dans le service.
- Path Params:
  - `id` (Long)
- Request Body: `PropertyUpdateDTO` (tous champs optionnels)
  - `title` (String)
  - `country` (String)
  - `city` (String)
  - `address` (String)
  - `longitude` (Double)
  - `latitude` (Double)
  - `description` (String)
  - `SqM` (Integer)
  - `typeOfProperty` (Enum PropertyType)
  - `typeOfRental` (Enum TypeOfRental)
  - `total_Rooms` (Integer)
  - `rentAmount` (Long)
  - `securityDeposit` (Long)
  - `isAvailable` (Boolean) — crucial pour la synchro on-chain
- Response:
  - `200 OK` avec `Property` (entité)
  - `404 Not Found` si introuvable
  - `403 Forbidden` si non autorisé
  - `500 Internal Server Error` en cas d’erreur serveur

---

### GET `/api/property-microservice/properties/{id}/availability`
Met à jour la disponibilité à `false` (note: c’est un `GET` mais agit comme une mise à jour côté service).

- Path Params:
  - `id` (Long)
- Response: `204 No Content`

---

### DELETE `/api/property-microservice/properties/{id}`
Retire la propriété on-chain et la marque inactive en base.

- Auth: Requiert l’adresse Ethereum du propriétaire; vérifications dans le service.
- Path Params:
  - `id` (Long)
- Response:
  - `204 No Content`
  - `404 Not Found`
  - `403 Forbidden`
  - `500 Internal Server Error`

---

## Chambres — `/api/property-microservice/rooms`

### GET `/api/property-microservice/rooms`
Liste toutes les chambres.

- Response: `200 OK` avec `List<RoomResponseDTO>`

`RoomResponseDTO`:
- `idRoom` (Long)
- `name` (String)
- `orderIndex` (Integer)
- `propertyId` (Long)

---

### GET `/api/property-microservice/rooms/{roomId}`
Récupère une chambre par ID.

- Path Params:
  - `roomId` (Long)
- Response:
  - `200 OK` avec `RoomResponseDTO`
  - `404 Not Found` si introuvable

---

### GET `/api/property-microservice/rooms/property/{propertyId}`
Liste les chambres d’une propriété spécifique.

- Path Params:
  - `propertyId` (Long)
- Response:
  - `200 OK` avec `List<RoomResponseDTO>`
  - `500 Internal Server Error` en cas d’erreur

---

### POST `/api/property-microservice/rooms/property/{propertyId}`
Crée une nouvelle chambre.

- Path Params:
  - `propertyId` (Long)
- Request Body: `RoomCreationDTO`
  - `name` (String, required)
  - `orderIndex` (Integer, required)
  - `imageIndexes` (List<Integer>, optional) — indices d’images éventuels
- Response:
  - `201 Created` avec `RoomResponseDTO`
  - `400 Bad Request` si la propriété parente n’existe pas
  - `500 Internal Server Error` en cas d’erreur

---

### PUT `/api/property-microservice/rooms/{roomId}`
Met à jour le nom et l’ordre d’une chambre.

- Path Params:
  - `roomId` (Long)
- Request Body: `RoomUpdateDTO`
  - `name` (String, optional)
  - `orderIndex` (Integer, optional)
- Response:
  - `200 OK` avec `RoomResponseDTO`
  - `404 Not Found`

---

### DELETE `/api/property-microservice/rooms/{roomId}`
Supprime une chambre et ses images associées.

- Path Params:
  - `roomId` (Long)
- Response:
  - `204 No Content`
  - `404 Not Found`

---

## Images de chambre — `/api/property-microservice/properties/room-images`

### GET `/api/property-microservice/properties/room-images/{imageId}`
Récupère les métadonnées d’une image par ID.

- Path Params:
  - `imageId` (Long)
- Response:
  - `200 OK` avec `RoomImageResponseDTO`
  - `404 Not Found`

`RoomImageResponseDTO`:
- `idImage` (Long)
- `url` (String)
- `s3Key` (String)
- `orderIndex` (Integer)
- `roomId` (Long)

---

### GET `/api/property-microservice/properties/room-images/room/{roomId}`
Liste les images associées à une chambre.

- Path Params:
  - `roomId` (Long)
- Response:
  - `200 OK` avec `List<RoomImageResponseDTO>`
  - `404 Not Found` si la chambre n’existe pas

---

### POST `/api/property-microservice/properties/room-images/room/{roomId}`
Upload une nouvelle image (multipart) et crée ses métadonnées.

- Path Params:
  - `roomId` (Long)
- Request (Content-Type: `multipart/form-data`):
  - `imageFile` (file, required)
  - `imageDto` (json part, required) — `RoomImageCreationDTO`
    - `orderIndex` (Integer, required)
- Response:
  - `201 Created` avec `RoomImageResponseDTO`
  - `404 Not Found` si la chambre n’existe pas
  - `500 Internal Server Error` en cas d’erreur

---

### PUT `/api/property-microservice/properties/room-images/{imageId}/order`
Met à jour l’ordre d’affichage d’une image.

- Path Params:
  - `imageId` (Long)
- Request Body: `RoomImageCreationDTO`
  - `orderIndex` (Integer, required)
- Response:
  - `200 OK` avec `RoomImageResponseDTO`
  - `404 Not Found`

---

### DELETE `/api/property-microservice/properties/room-images/{imageId}`
Supprime les métadonnées de l’image et le fichier du stockage.

- Path Params:
  - `imageId` (Long)
- Response:
  - `204 No Content`
  - `404 Not Found`

---

## Enums de référence

- `PropertyType`: Type de propriété (appartement, maison, etc.)
- `TypeOfRental`: Type de location (ex: longue durée, courte durée). Les valeurs exactes sont définies dans le code.

---

## Remarques complémentaires

- Le champ `isAvailable` de la propriété est important pour la synchronisation avec le smart contract.
- Certaines routes utilisent des choix de méthode atypiques (ex: `GET /{id}/availability` change l’état). En production, un `PATCH` ou `PUT` serait plus approprié.
- Les contrôleurs retournent parfois l’entité (`Property`) au lieu du DTO pour la mise à jour; gardez cela en tête côté client.

## Exemples de payloads

### PropertyCreationDTO (JSON)
{
  "onChainId": 123,
  "title": "Bel appartement centre-ville",
  "country": "France",
  "city": "Paris",
  "address": "10 Rue de Rivoli",
  "longitude": 2.3522,
  "latitude": 48.8566,
  "description": "2 pièces lumineux",
  "sqM": 45,
  "typeOfProperty": "APARTMENT",
  "typeOfRental": "LONG_TERM",
  "rentAmount": 1200,
  "securityDeposit": 2400
}

### PropertyUpdateDTO (JSON — tous champs optionnels)
{
  "title": "Bel appartement rénové",
  "description": "2 pièces entièrement refait",
  "SqM": 47,
  "rentAmount": 1250,
  "isAvailable": true
}

### PropertySearchDTO (JSON)
{
  "city": "Paris",
  "minRentAmount": 1000,
  "maxRentAmount": 1500,
  "typeOfRental": "LONG_TERM",
  "latitude": 48.8566,
  "longitude": 2.3522,
  "radiusInKm": 5.0
}

### RoomCreationDTO (JSON)
{
  "name": "Chambre principale",
  "orderIndex": 1,
  "imageIndexes": [1, 2, 3]
}

### RoomUpdateDTO (JSON)
{
  "name": "Chambre parentale",
  "orderIndex": 2
}

### RoomImageCreationDTO (JSON)
{
  "orderIndex": 1
}

