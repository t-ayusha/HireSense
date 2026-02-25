package com.resume2role.controller;

import com.resume2role.model.Resume;
import com.resume2role.service.ResumeService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping("/upload")
    public Resume uploadResume(
            @RequestParam String userId,
            @RequestParam MultipartFile file
    ) throws Exception {
        return resumeService.uploadResume(userId, file);
    }
}