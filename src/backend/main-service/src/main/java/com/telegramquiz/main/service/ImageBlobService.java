package com.telegramquiz.main.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.telegramquiz.main.entity.ImageBlob;
import com.telegramquiz.main.repository.ImageBlobRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageBlobService {

    private final ImageBlobRepository imageBlobRepository;

    private static final int MAX_DIMENSION = 1920;
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    public Long store(MultipartFile file) throws IOException {
        String contentType = file.getContentType() != null
                ? file.getContentType()
                : "application/octet-stream";

        byte[] data = file.getBytes();

        if (contentType.startsWith("image/")) {
            data = resizeImage(data, contentType);
        }

        ImageBlob blob = ImageBlob.builder()
                .data(data)
                .contentType(contentType)
                .build();
        return imageBlobRepository.save(blob).getId();
    }

    private byte[] resizeImage(byte[] data, String contentType) throws IOException {
        if (data.length <= MAX_FILE_SIZE) {
            return data;
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(new ByteArrayInputStream(data))
                    .size(MAX_DIMENSION, MAX_DIMENSION)
                    .keepAspectRatio(true)
                    .outputQuality(0.85)
                    .outputFormat(getFormatName(contentType))
                    .toOutputStream(outputStream);
            
            byte[] resized = outputStream.toByteArray();
            log.debug("Resized image from {} bytes to {} bytes", data.length, resized.length);
            return resized;
        } catch (Exception e) {
            log.warn("Failed to resize image, using original: {}", e.getMessage());
            return data;
        }
    }

    private String getFormatName(String contentType) {
        if (contentType.contains("png")) return "png";
        if (contentType.contains("gif")) return "gif";
        if (contentType.contains("webp")) return "webp";
        return "jpg";
    }

    public ImageBlob findById(Long id) {
        return imageBlobRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Image not found: " + id));
    }

    public String toBase64DataUri(Long id) {
        ImageBlob blob = findById(id);
        String encoded = Base64.getEncoder().encodeToString(blob.getData());
        return "data:" + blob.getContentType() + ";base64," + encoded;
    }

    public Long storeFromBase64DataUri(String dataUri) {
        if (dataUri == null || !dataUri.startsWith("data:")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid data URI");
        }
        int commaIdx = dataUri.indexOf(',');
        if (commaIdx < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Malformed data URI");
        }
        String meta = dataUri.substring(5, commaIdx);
        String base64Part = dataUri.substring(commaIdx + 1);

        String contentType = "application/octet-stream";
        if (meta.endsWith(";base64")) {
            contentType = meta.substring(0, meta.length() - 7);
        }

        byte[] data;
        try {
            data = Base64.getDecoder().decode(base64Part);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid base64 data");
        }

        if (contentType.startsWith("image/")) {
            try {
                data = resizeImage(data, contentType);
            } catch (IOException e) {
                log.warn("Failed to resize image from base64: {}", e.getMessage());
            }
        }

        ImageBlob blob = ImageBlob.builder()
                .data(data)
                .contentType(contentType)
                .build();
        return imageBlobRepository.save(blob).getId();
    }
}
