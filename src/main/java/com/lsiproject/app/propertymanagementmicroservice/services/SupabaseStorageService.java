package com.lsiproject.app.propertymanagementmicroservice.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;


import java.io.IOException;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    private final WebClient webClient;
    private final String bucketName;
    private final String supabaseUrl;
    private final String supabaseAnonKey;

    public SupabaseStorageService(
            WebClient.Builder webClientBuilder,
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.anon-key}") String anonKey,
            @Value("${supabase.storage.bucket-name}") String bucket
    ) {
        this.supabaseUrl = supabaseUrl;
        this.supabaseAnonKey = anonKey;
        this.bucketName = bucket;


        this.webClient = webClientBuilder
                .baseUrl(supabaseUrl + "/storage/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + anonKey)
                .defaultHeader("apikey", anonKey)
                .build();
    }

    /**
     * Upload a MultipartFile directly to Supabase Storage.
     */
    public String uploadImageFile(MultipartFile file, Long propertyId, String roomName) throws IOException {
        //  Build clean, safe names
        String cleanRoom = roomName.replaceAll("[^a-zA-Z0-9\\-_]", "-").toLowerCase();
        String fileExt = getFileExtension(file.getOriginalFilename());
        String uniqueFileName = UUID.randomUUID() + "." + fileExt;

        // Object path inside bucket — DO NOT encode slashes
        String objectPath = propertyId + "/" + cleanRoom + "/" + uniqueFileName;


        // Upload file to Supabase
        try {
            String response = webClient.post()
                    .uri("/object/" + bucketName + "/" + objectPath)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseAnonKey)
                    .header("apikey", supabaseAnonKey)
                    .contentType(MediaType.parseMediaType(file.getContentType()))
                    .bodyValue(file.getBytes())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("Supabase response: " + response);
        } catch (Exception e) {
            System.err.println("Supabase upload failed: " + e.getMessage());
            throw new RuntimeException("Upload to Supabase failed: " + e.getMessage(), e);
        }

        //Construct public URL
        return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + objectPath;
    }

    public void deleteFile(String objectPath) {

        String jsonBody = String.format("{\"prefixes\": [\"%s\"]}", objectPath);

        webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/object/" + bucketName + "/remove")
                        .build()
                )
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(jsonBody)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }


    private String getFileExtension(String name) {
        if (name == null || !name.contains(".")) {
            return "jpg";
        }
        return name.substring(name.lastIndexOf(".") + 1);
    }


}


