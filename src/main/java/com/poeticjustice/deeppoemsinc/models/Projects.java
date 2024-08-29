package com.poeticjustice.deeppoemsinc.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
// import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import jakarta.persistence.Id;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString(includeFieldNames=true)
@Data
// @RequiredArgsConstructor
@Slf4j
// @Table(name = "projects")
public class Projects {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long projectId;

    private String projectName;
    private String description;
    private BigDecimal targetAmount;
    private BigDecimal collectedAmount;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and Setters

    public enum Status {
        ACTIVE,
        COMPLETED
    }   
}
