package com.antock.api.file.infrastructure;

import com.antock.api.file.domain.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaFileRepository extends JpaRepository<File, Long> {

    @Query("SELECT f FROM File f WHERE " +
            "LOWER(f.metadata.originalFileName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.description.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<File> findByKeyword(@Param("keyword") String keyword);

    Optional<File> findByMetadataStoredFileName(String storedFileName);
}