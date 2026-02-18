package com.solutiongameofficial.io;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceLoader {

    private static final int DEFAULT_BUFFER_SIZE = 64 * 1024;

    public static InputStream open(String path) {
        String normalized = path.startsWith("/") ? path.substring(1) : path;
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(normalized);

        if (stream == null) {
            throw new IllegalStateException("Resource not found: " + path);
        }

        return stream;
    }

    public static byte[] readAllBytes(String path) {
        try (InputStream inputStream = open(path);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read resource bytes: " + path, exception);
        }
    }

    public static BufferedImage loadImage(String path) {
        try (InputStream inputStream = open(path)) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new IllegalStateException("Unsupported image format or corrupt resource: " + path);
            }
            return image;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read resource image: " + path, exception);
        }
    }

    public static List<BufferedImage> loadAllImagesFromRaw() {
        Path rawDir = getProjectResourceDir().resolve("raw");
        if (!Files.exists(rawDir)) {
            throw new IllegalStateException("Raw directory not found: " + rawDir.toAbsolutePath());
        }

        try {
            List<BufferedImage> result = new ArrayList<>();

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(rawDir)) {
                for (Path file : stream) {
                    if (!Files.isRegularFile(file)) {
                        continue;
                    }

                    String name = file.getFileName().toString();
                    if (!isImage(name)) {
                        continue;
                    }

                    BufferedImage image = ImageIO.read(file.toFile());
                    if (image == null) {
                        throw new IllegalStateException("Unsupported image format or corrupt file: " + file);
                    }

                    result.add(image);
                }
            }

            return result;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load images from raw folder: " + rawDir, exception);
        }
    }

    public static void saveAllImagesToAsciiFolder(List<BufferedImage> images) {
        Path asciiDir = ensureAsciiDirExists();

        try {
            int index = 1;
            for (BufferedImage image : images) {
                String fileName = String.format(Locale.ROOT, "ascii_%04d.png", index++);
                Path outFile = asciiDir.resolve(fileName);
                ImageIO.write(image, "png", outFile.toFile());
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to save ascii images into: " + asciiDir, exception);
        }
    }

    private static Path getProjectResourceDir() {
        return Paths.get(System.getProperty("user.dir"))
                .resolve("src")
                .resolve("main")
                .resolve("resources");
    }

    private static Path ensureAsciiDirExists() {
        Path asciiDir = getProjectResourceDir().resolve("ascii");
        try {
            Files.createDirectories(asciiDir);
            return asciiDir;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create ascii directory: " + asciiDir, exception);
        }
    }

    private static boolean isImage(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".png")
                || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg");
    }
}