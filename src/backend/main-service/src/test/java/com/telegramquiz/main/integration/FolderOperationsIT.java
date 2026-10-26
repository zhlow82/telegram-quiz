package com.telegramquiz.main.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telegramquiz.main.dto.FolderReorderRequestDto;
import com.telegramquiz.main.dto.FolderRequestDto;
import com.telegramquiz.main.entity.Folder;
import com.telegramquiz.main.repository.FolderRepository;

@DisplayName("folders-sharing / Flat folders and ordering")
class FolderOperationsIT extends FolderIntegrationTestBase {

    @Autowired
    private FolderRepository folderRepository;

    @BeforeEach
    void setUp() {
        folderRepository.deleteAll();
    }

    @Test
    @DisplayName("Creating a folder")
    void createFolderAppendedToEnd() throws Exception {
        Folder first = Folder.builder()
                .name("First")
                .createdBy("alice")
                .sortOrder(0)
                .build();
        folderRepository.save(first);

        mockMvc.perform(post("/api/folders")
                        .with(asUser("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new FolderRequestDto("Second"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Second"));
        
        // Verify the order in the database
        List<Folder> folders = folderRepository.findAllByCreatedByOrderBySortOrderAscCreatedAtAsc("alice");
        assert folders.size() == 2;
        assert folders.get(0).getName().equals("First");
        assert folders.get(1).getName().equals("Second");
        assert folders.get(0).getSortOrder() == 0;
        assert folders.get(1).getSortOrder() == 1;
    }

    @Test
    @DisplayName("Reordering folders")
    void reorderFoldersPersisted() throws Exception {
        Folder folder1 = Folder.builder()
                .name("Folder 1")
                .createdBy("alice")
                .sortOrder(0)
                .build();
        Folder folder2 = Folder.builder()
                .name("Folder 2")
                .createdBy("alice")
                .sortOrder(1)
                .build();
        folderRepository.saveAll(List.of(folder1, folder2));

        FolderReorderRequestDto reorderRequest = new FolderReorderRequestDto(
                List.of(folder2.getId(), folder1.getId()));

        mockMvc.perform(patch("/api/folders/reorder")
                        .with(asUser("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reorderRequest)))
                .andExpect(status().isNoContent());

        List<Folder> folders = folderRepository.findAllByCreatedByOrderBySortOrderAscCreatedAtAsc("alice");
        assert folders.get(0).getId().equals(folder2.getId());
        assert folders.get(1).getId().equals(folder1.getId());
    }
}
