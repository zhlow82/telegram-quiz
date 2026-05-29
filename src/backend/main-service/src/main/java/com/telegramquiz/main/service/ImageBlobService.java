package com.telegramquiz.main.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
}
