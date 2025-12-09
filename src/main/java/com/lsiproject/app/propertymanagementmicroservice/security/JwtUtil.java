package com.lsiproject.app.propertymanagementmicroservice.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class JwtUtil {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Extrait le Payload du JWT, le décode de Base64 et le parse en Map de Claims.
     */
    public Map<String, Object> extractClaimsFromPayload(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            String payloadEncoded = parts[1];
            String payloadPadded = payloadEncoded.replace('-', '+').replace('_', '/');
            while (payloadPadded.length() % 4 != 0) {
                payloadPadded += "=";
            }

            byte[] decodedBytes = Base64.getDecoder().decode(payloadPadded);
            String payloadJson = new String(decodedBytes, StandardCharsets.UTF_8);

            return objectMapper.readValue(payloadJson, Map.class);

        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException | JsonProcessingException e) {
            System.err.println("Erreur decoding JWT Payload: " + e.getMessage());
            return null;
        }
    }

    /**
     * Adapté pour la structure spécifique du token OAuth2/OIDC fourni.
     */
    public UserPrincipal extractUserPrincipal(String token) {
        Map<String, Object> claims = extractClaimsFromPayload(token);
        if (claims == null) {
            return null;
        }

        try {
            // 1. Extraire l'adresse du portefeuille
            // Le token contient "sub" et "wallet" avec la même valeur.
            // On priorise "sub" car c'est le standard JWT.
            String walletAddress = String.valueOf(claims.getOrDefault("sub", claims.get("wallet")));

            // 2. Extraire l'ID (Format: "id": 1)
            // Attention: claims.get("id") renvoie un Integer, on le convertit en String puis en Long pour la sécurité
            Long idUser = null;
            if (claims.get("id") != null) {
                idUser = Long.valueOf(claims.get("id").toString());
            }

            // 3. Extraire le Rôle (Format: "role": "ROLE_USER")
            Set<String> roles = new HashSet<>();
            Object roleClaim = claims.get("role");

            if (roleClaim != null) {
                String roleStr = roleClaim.toString();

                // IMPORTANT: Votre classe UserPrincipal ajoute déjà "ROLE_" dans son constructeur.
                // Si le token contient déjà "ROLE_USER", UserPrincipal créerait "ROLE_ROLE_USER".
                // Nous devons donc nettoyer la chaîne ici.
                if (roleStr.startsWith("ROLE_")) {
                    roleStr = roleStr.replace("ROLE_", "");
                }
                roles.add(roleStr);
            }

            // Note: Le token contient aussi 'email', vous pouvez modifier UserPrincipal pour le stocker si nécessaire.

            return new UserPrincipal(idUser, walletAddress, roles);

        } catch (Exception e) {
            System.err.println("Erreur de conversion des Claims en UserPrincipal: " + e.getMessage());
            e.printStackTrace(); // Utile pour le debug
            return null;
        }
    }
}