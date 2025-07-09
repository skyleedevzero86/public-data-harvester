package com.antock.api.coseller.domain;

import com.antock.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;

import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorpMastHistory extends BaseEntity {
    private String username;
    private String action;
    private String bizNo;
    private String result;
    @Lob
    @Column
    private String message;
    private LocalDateTime timestamp;
}