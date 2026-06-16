package com.example.loudhotel.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class FileUploadUtil {

    private static final String UPLOAD_DIR = "uploads/";

    public static String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));

            String originalName = file.getOriginalFilename();
            String extension = "";

            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }

            String fileName = UUID.randomUUID() + extension;
            Path filePath = Paths.get(UPLOAD_DIR + fileName);

            Files.write(filePath, file.getBytes());

            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Could not upload file", e);
        }
    }

    public static void deleteFile(String fullPath) {
        try {
            Path path = Paths.get(fullPath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("Could not delete file", e);
        }
    }

    public static String upload(MultipartFile file, String folder) {

        try {

            Files.createDirectories(Paths.get(folder));

            String originalName = file.getOriginalFilename();
            String extension = "";

            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }

            String fileName = UUID.randomUUID() + extension;

            Path filePath = Paths.get(folder, fileName);

            Files.write(filePath, file.getBytes());

            return fileName;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
