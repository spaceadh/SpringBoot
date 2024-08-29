package com.poeticjustice.deeppoemsinc.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
// @Table(name = "donations")
public class Donations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long donationId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private DonationAppUser user;

    private BigDecimal amount;
    private LocalDateTime donationDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private DonationAppUser recipient;

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED
    }   
}
