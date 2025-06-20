package com.dnofulla.marketcove.backend_api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

/**
 * Service for handling S3 image uploads and management
 * This service is temporarily disabled until AWS credentials are configured
 */
@Service
@Slf4j
public class S3ImageService {

    @Value("${aws.s3.bucket-name:marketcove-images}")
    private String bucketName;

    @Value("${aws.s3.region:us-east-1}")
    private String region;

    @Value("${aws.s3.enabled:false}")
    private boolean s3Enabled;

    private S3Client s3Client;

    /**
     * Initialize S3 client if enabled
     */
    private void initializeS3Client() {
        if (s3Enabled && s3Client == null) {
            try {
                s3Client = S3Client.builder()
                        .region(Region.of(region))
                        .credentialsProvider(DefaultCredentialsProvider.create())
                        .build();
                log.info("S3 client initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize S3 client: {}", e.getMessage());
                s3Enabled = false;
            }
        }
    }

    /**
     * Upload an image to S3
     * 
     * @param file   The image file to upload
     * @param folder The folder path (e.g., "storefronts", "items")
     * @return The S3 URL of the uploaded image, or null if S3 is disabled
     */
    public String uploadImage(MultipartFile file, String folder) {
        if (!s3Enabled) {
            log.warn("S3 upload attempted but S3 is disabled. File: {}", file.getOriginalFilename());
            return generateMockImageUrl(file.getOriginalFilename(), folder);
        }

        try {
            initializeS3Client();

            if (s3Client == null) {
                log.error("S3 client not available");
                return generateMockImageUrl(file.getOriginalFilename(), folder);
            }

            String fileName = generateFileName(file.getOriginalFilename());
            String key = folder + "/" + fileName;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            String imageUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
            log.info("Successfully uploaded image to S3: {}", imageUrl);
            return imageUrl;

        } catch (IOException e) {
            log.error("Error reading file for S3 upload: {}", e.getMessage());
            return generateMockImageUrl(file.getOriginalFilename(), folder);
        } catch (Exception e) {
            log.error("Error uploading to S3: {}", e.getMessage());
            return generateMockImageUrl(file.getOriginalFilename(), folder);
        }
    }

    /**
     * Delete an image from S3
     * 
     * @param imageUrl The full S3 URL of the image to delete
     * @return true if deleted successfully or if S3 is disabled, false otherwise
     */
    public boolean deleteImage(String imageUrl) {
        if (!s3Enabled) {
            log.warn("S3 delete attempted but S3 is disabled. URL: {}", imageUrl);
            return true; // Return true for mock implementation
        }

        try {
            initializeS3Client();

            if (s3Client == null) {
                log.error("S3 client not available for deletion");
                return false;
            }

            String key = extractKeyFromUrl(imageUrl);
            if (key == null) {
                log.error("Could not extract key from URL: {}", imageUrl);
                return false;
            }

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Successfully deleted image from S3: {}", imageUrl);
            return true;

        } catch (Exception e) {
            log.error("Error deleting image from S3: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Generate a unique filename for the uploaded file
     */
    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    /**
     * Extract the S3 key from a full S3 URL
     */
    private String extractKeyFromUrl(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains(bucketName)) {
            return null;
        }

        try {
            String baseUrl = String.format("https://%s.s3.%s.amazonaws.com/", bucketName, region);
            if (imageUrl.startsWith(baseUrl)) {
                return imageUrl.substring(baseUrl.length());
            }
        } catch (Exception e) {
            log.error("Error extracting key from URL: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Generate a mock image URL when S3 is disabled
     * This helps with development when AWS credentials are not available
     */
    private String generateMockImageUrl(String originalFilename, String folder) {
        String fileName = generateFileName(originalFilename);
        return String.format("https://mock-images.marketcove.com/%s/%s", folder, fileName);
    }

    /**
     * Check if S3 service is enabled and properly configured
     */
    public boolean isS3Enabled() {
        return s3Enabled;
    }

    /**
     * Validate image file
     */
    public boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        return contentType.startsWith("image/") &&
                (contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/gif") ||
                        contentType.equals("image/webp"));
    }

    /**
     * Get maximum file size in bytes (5MB)
     */
    public long getMaxFileSize() {
        return 5 * 1024 * 1024; // 5MB
    }
}