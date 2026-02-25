package com.resume2role.repository;

import com.resume2role.model.Resume;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ResumeRepository extends MongoRepository<Resume, String> {

    List<Resume> findByUserId(String userId);
}