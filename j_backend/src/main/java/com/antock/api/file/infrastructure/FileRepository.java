package com.antock.api.file.infrastructure;


import com.antock.api.file.domain.File;
import java.util.List;
import java.util.Optional;

public interface FileRepository {
    File save(File file);
    Optional<File> findById(Long id);
    List<File> findAll();
    List<File> findByKeyword(String keyword);
    Optional<File> findByStoredFileName(String storedFileName);
    void deleteById(Long id);
    void delete(File file);
    boolean existsById(Long id);
}