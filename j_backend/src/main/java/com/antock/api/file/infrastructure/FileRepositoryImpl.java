package com.antock.api.file.infrastructure;

import com.antock.api.file.domain.File;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FileRepositoryImpl implements FileRepository {

    private final JpaFileRepository jpaFileRepository;

    @Override
    public File save(File file) {
        return jpaFileRepository.save(file);
    }

    @Override
    public Optional<File> findById(Long id) {
        return jpaFileRepository.findById(id);
    }

    @Override
    public List<File> findAll() {
        return jpaFileRepository.findAll();
    }

    @Override
    public List<File> findByKeyword(String keyword) {
        return jpaFileRepository.findByKeyword(keyword);
    }

    @Override
    public Optional<File> findByStoredFileName(String storedFileName) {
        return jpaFileRepository.findByMetadataStoredFileName(storedFileName);
    }

    @Override
    public void deleteById(Long id) {
        jpaFileRepository.deleteById(id);
    }

    @Override
    public void delete(File file) {
        jpaFileRepository.delete(file);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaFileRepository.existsById(id);
    }
}