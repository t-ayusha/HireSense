package com.resume2role.service;

import com.resume2role.model.TechnicalProfile;
import com.resume2role.model.SkillSet;
import com.resume2role.model.Resume;
import com.resume2role.repository.ResumeRepository;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ResumeService {

    private final RuleBasedRolePredictor rulePredictor;
    private final LLMRolePredictor llmPredictor;
    private final ResumeRepository resumeRepository;
    private final SkillExtractor skillExtractor;

    public ResumeService(ResumeRepository resumeRepository,
                         SkillExtractor skillExtractor,
                         RuleBasedRolePredictor rulePredictor,
                         LLMRolePredictor llmPredictor) {
        this.resumeRepository = resumeRepository;
        this.skillExtractor = skillExtractor;
        this.rulePredictor = rulePredictor;
        this.llmPredictor = llmPredictor;
    }

    public Resume uploadResume(String userId, MultipartFile file) throws Exception {

        String uploadDir = System.getProperty("user.dir") + "/uploads/resumes/";
        File folder = new File(uploadDir);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String originalName = file.getOriginalFilename().replaceAll("\\s+", "_");
        String uniqueFileName = UUID.randomUUID() + "_" + originalName;

        File dest = new File(uploadDir + uniqueFileName);
        file.transferTo(dest);

        Tika tika = new Tika();
        String extractedText = tika.parseToString(dest);

        Map<String, Object> parsedData = parseResume(extractedText);

        SkillSet skills = skillExtractor.extractSkills(extractedText);

        String ruleRole = rulePredictor.predictRole(skills);
        String llmRole = llmPredictor.predictRole(extractedText);

        String finalRole;

        if (!llmRole.equalsIgnoreCase("Software Developer"))
            finalRole = llmRole;
        else
            finalRole = ruleRole;
        TechnicalProfile technicalProfile = TechnicalProfile.builder()
                .skills(skills)
                .projects(new ArrayList<>())
                .predictedRole(finalRole)
                .experienceLevel("Unknown")
                .score(0)
                .build();

        Resume resume = Resume.builder()
                .userId(userId)
                .fileName(uniqueFileName)
                .filePath(dest.getPath())
                .extractedText(extractedText)
                .parsedData(parsedData)
                .uploadedAt(LocalDateTime.now())
                .technicalProfile(technicalProfile)
                .build();

        return resumeRepository.save(resume);
    }

    private Map<String, Object> parseResume(String text) {
        Map<String, Object> data = new HashMap<>();

        String email = text.replaceAll("(?s).*?(\\b[\\w.-]+@[\\w.-]+\\.\\w+\\b).*", "$1");
        data.put("email", email);

        return data;
    }
}