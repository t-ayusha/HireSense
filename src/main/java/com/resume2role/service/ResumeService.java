package com.resume2role.service;

import com.resume2role.model.Resume;
import com.resume2role.repository.ResumeRepository;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;

    public ResumeService(ResumeRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

    public Resume uploadResume(String userId, MultipartFile file) throws Exception {

        // ===== 1. Ensure upload directory exists =====
        String uploadDir = System.getProperty("user.dir") + "/uploads/resumes/";        File folder = new File(uploadDir);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // ===== 2. Create unique & safe filename =====
        String originalName = file.getOriginalFilename().replaceAll("\\s+", "_");
        String uniqueFileName = UUID.randomUUID() + "_" + originalName;

        File dest = new File(uploadDir + uniqueFileName);

        // ===== 3. Save file locally =====
        file.transferTo(dest);

        // ===== 4. Extract text using Apache Tika =====
        Tika tika = new Tika();
        String extractedText = tika.parseToString(dest);

        // ===== 5. Basic parsing =====
        Map<String, Object> parsedData = parseResume(extractedText);

        // ===== 6. Save to MongoDB =====
        Resume resume = Resume.builder()
                .userId(userId)
                .fileName(uniqueFileName)
                .filePath(dest.getPath())
                .extractedText(extractedText)
                .parsedData(parsedData)
                .uploadedAt(LocalDateTime.now())
                .build();

        return resumeRepository.save(resume);
    }

    private Map<String, Object> parseResume(String text) {
        Map<String, Object> data = new HashMap<>();

        // Simple email extraction
        String email = text.replaceAll("(?s).*?(\\b[\\w.-]+@[\\w.-]+\\.\\w+\\b).*", "$1");
        data.put("email", email);

        return data;
    }
}