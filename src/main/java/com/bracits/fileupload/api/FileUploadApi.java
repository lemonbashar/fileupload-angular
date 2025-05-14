package com.bracits.fileupload.api;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1")
public class FileUploadApi {

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "File is empty!"));
        }

        try {
            ResponseEntity<Map<String, String>> INTERNAL_SERVER_ERROR = saveUploadedFile(file);
            if (INTERNAL_SERVER_ERROR != null) return INTERNAL_SERVER_ERROR;

            return ResponseEntity.status(HttpStatus.OK).body(Map.of("imageUrl", "http://localhost:8080/api/v1/download/" + file.getOriginalFilename()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }

    private static ResponseEntity<Map<String, String>> saveUploadedFile(MultipartFile file) throws FileNotFoundException {
        // Define the directory to save the uploaded file
        String uploadDir = "uploads/";
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Save the file to the directory
        File destinationFile = new File(uploadDir + file.getOriginalFilename());
        FileOutputStream f = new FileOutputStream(destinationFile);
        try(f) {
            // File saved successfully
            f.write(file.getBytes());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
        return null;
    }

    @PostMapping("/upload/ck-editor")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("upload") MultipartFile file) throws IOException {
        Map<String, Object> response = new HashMap<>();

        saveUploadedFile(file);

        // Return response for CKEditor
        response.put("uploaded", 1);
        response.put("fileName", file.getOriginalFilename());
        response.put("url", "http://localhost:8080/api/v1/download/" + file.getOriginalFilename());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<?> downloadFile(@PathVariable("filename") String filename) {
        try {
            // Define the directory where files are stored
            String uploadDir = "uploads/";
            File file = new File(uploadDir + filename);

            if (!file.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
            }

            // Return the file as a resource
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
                    .body(org.springframework.util.StreamUtils.copyToByteArray(new java.io.FileInputStream(file)));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File download failed: " + e.getMessage());
        }
    }
}
