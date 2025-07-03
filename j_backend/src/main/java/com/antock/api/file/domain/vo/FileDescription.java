package com.antock.api.file.domain.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileDescription {

    @Column(length = 1000)
    private String description;

    public static FileDescription of(String description) {
        return FileDescription.builder()
                .description(description != null ? description.trim() : "")
                .build();
    }

    public boolean isEmpty() {
        return description == null || description.trim().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileDescription that = (FileDescription) o;
        return Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description);
    }
}