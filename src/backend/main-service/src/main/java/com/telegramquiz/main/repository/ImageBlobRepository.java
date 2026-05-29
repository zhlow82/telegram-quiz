package com.telegramquiz.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.telegramquiz.main.entity.ImageBlob;

public interface ImageBlobRepository extends JpaRepository<ImageBlob, Long> {
}
