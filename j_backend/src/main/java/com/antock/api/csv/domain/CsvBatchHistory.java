package com.antock.api.csv.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class CsvBatchHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String city;
    private String district;
    private String fileName;
    private int recordCount;
    private String status;
    private String message;
    private LocalDateTime timestamp;
}