package com.example.wx.utils;

import com.example.wx.exceptions.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;

/**
 * @author wangx
 * @description
 * @create 2025/7/20 16:05
 */
public final class FilesUtils {
    private static final Logger logger = LoggerFactory.getLogger(FilesUtils.class);

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private static final Random RANDOM = new Random();

    private FilesUtils() {
    }

    private static String generateRandomFileName(String originalFilename) {

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }

        return generateRandomString() + extension;
    }

    private static String generateRandomString() {
        StringBuilder result = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            result.append(CHARACTERS.charAt(index));
        }
        return result.toString();
    }

    public static void initTmpFolder(String path) {
        if (path == null || path.isEmpty()) {

            throw new AppException("path is null or empty");
        }
        File tmpFolder = new File(path);
        if (!tmpFolder.exists()) {
            tmpFolder.mkdirs();
        }

        logger.info("Init tmp folder: {}", tmpFolder.getAbsolutePath());
    }

    public static void deleteDirectory(File directory) {

        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }

            directory.delete();
        }
    }

    /**
     * save file to tmp folder
     */
    public static String saveTempFile(MultipartFile file, String path) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new AppException("File is null or empty");
        }

        String randomFileName = generateRandomFileName(Objects.requireNonNull(file.getOriginalFilename()));
        String filePath = System.getProperty("user.dir") + path + randomFileName;

        file.transferTo(new File(filePath));

        return filePath;
    }
}
