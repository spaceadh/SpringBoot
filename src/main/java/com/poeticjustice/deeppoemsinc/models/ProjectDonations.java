package com.poeticjustice.deeppoemsinc.models;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
// import jakarta.persistence.EnumType;
// import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
// import jakarta.persistence.Table;
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
// @Table(name = "project_donations")
public class ProjectDonations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long projectDonationId;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Projects project;

    @ManyToOne
    @JoinColumn(name = "donation_id", nullable = false)
    private Donations donation;

    private BigDecimal allocatedAmount;
}
