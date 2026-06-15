package com.telegramquiz.main.service;

import java.io.IOException;
import java.util.Base64;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.telegramquiz.main.entity.ImageBlob;
import com.telegramquiz.main.repository.ImageBlobRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImageBlobService {

    private final ImageBlobRepository imageBlobRepository;

    public Long store(MultipartFile file) throws IOException {
        String contentType = file.getContentType() != null
                ? file.getContentType()
                : "application/octet-stream";
        ImageBlob blob = ImageBlob.builder()
                .data(file.getBytes())
                .contentType(contentType)
                .build();
        return imageBlobRepository.save(blob).getId();
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

        ImageBlob blob = ImageBlob.builder()
                .data(data)
                .contentType(contentType)
                .build();
        return imageBlobRepository.save(blob).getId();
    }
}
