package com.lsiproject.app.propertymanagementmicroservice.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.apache.tika.Tika;

import java.util.UUID;

@Service
public class SupabaseStorageService {

    private static final Tika tika = new Tika();
    private final WebClient webClient;
    private final String BUCKET_NAME;

    // --- Configuration from application.properties ---
    private String supabaseUrl;

    private String supabaseAnonKey;

    public SupabaseStorageService(
            @Value("${supabase.storage.bucket-name}") String BUCKET_NAME,
            WebClient.Builder webClientBuilder,
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.anon-key}") String supabaseAnonKey) {

        System.out.println("DEBUG: Successfully Injected Supabase URL: " + supabaseUrl);

        if (supabaseUrl == null || supabaseUrl.isBlank()) {
            throw new IllegalArgumentException("Configuration property 'supabase.url' is missing or empty.");
        }
        this.BUCKET_NAME = BUCKET_NAME;
        this.supabaseUrl = supabaseUrl;
        this.supabaseAnonKey = supabaseAnonKey;

        // Initialize WebClient
        this.webClient = webClientBuilder.baseUrl(supabaseUrl + "/storage/v1").build();
    }

    /**
     * Uploads a MultipartFile to the Supabase Storage bucket using WebClient.
     * * @param file The file to upload.
     * @param propertyId The ID of the property.
     * @param roomFolder The sanitized room name folder.
     * @return The public URL of the uploaded file.
     * @throws Exception if the upload or file conversion fails.
     */
    public String uploadImageFile(MultipartFile file, Long propertyId, String roomFolder) throws Exception {
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty.");
        }

        String fileExtension = fileName.substring(fileName.lastIndexOf('.'));
        String uniqueFileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + fileExtension;

        String filePath = String.format("%d/%s/%s", propertyId, roomFolder, uniqueFileName );
        String apiPath = String.format("/object/%s/%s", BUCKET_NAME, filePath);
        String contentType = tika.detect(file.getBytes(), fileName);


        try {
            String response = this.webClient.post()
                    .uri(apiPath)
                    .header("Authorization", "Bearer " + supabaseAnonKey)
                    .header("Content-Type", contentType)
                    .bodyValue(file.getBytes())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .map(errorBody -> {
                                        System.err.println("Error body: " + errorBody);
                                        return new RuntimeException("Supabase upload failed: " + errorBody);
                                    }))
                    .bodyToMono(String.class)
                    .block();

            System.out.println("Upload response: " + response);

        } catch (WebClientResponseException e) {
            System.err.println("=== ERROR DETAILS ===");
            System.err.println("Status: " + e.getStatusCode());
            System.err.println("Response body: " + e.getResponseBodyAsString());
            System.err.println("Headers: " + e.getHeaders());
            System.err.println("====================");
            throw new Exception("Failed to upload image to Supabase: " + e.getMessage(), e);
        }

        String publicUrl = String.format("%s/storage/v1/object/public/%s/%s",
                supabaseUrl, BUCKET_NAME, filePath);

        return publicUrl;
    }

    public void deleteFile(String objectPath) throws Exception {
        if (objectPath == null || objectPath.isEmpty()) {
            throw new IllegalArgumentException("Object path cannot be empty.");
        }

        // API path for DELETE: /object/{bucketName}/{filePath}
        String apiPath = String.format("/object/%s/%s", BUCKET_NAME, objectPath);

        try {
            String response = this.webClient.delete()
                    .uri(apiPath)
                    .header("Authorization", "Bearer " + supabaseAnonKey)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .map(errorBody -> {
                                        System.err.println("Error body: " + errorBody);
                                        return new RuntimeException("Supabase delete failed: " + errorBody);
                                    }))
                    .bodyToMono(String.class)
                    .block();

            System.out.println("✅ File deleted successfully from Supabase: " + objectPath);
            System.out.println("Delete response: " + response);

        } catch (WebClientResponseException e) {
            System.err.println("=== DELETE ERROR DETAILS ===");
            System.err.println("Status: " + e.getStatusCode());
            System.err.println("Response body: " + e.getResponseBodyAsString());
            System.err.println("============================");
            throw new Exception("Failed to delete file from Supabase: " + e.getMessage(), e);
        }
    }
}