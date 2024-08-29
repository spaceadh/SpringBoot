package com.poeticjustice.deeppoemsinc.models;

import java.security.NoSuchAlgorithmException;
// import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Entity
@Data
@Slf4j
@Table(name = "donation_app_user")
public class DonationAppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @NotBlank
    private String firstName;
    
    @NotBlank
    private String lastName;

    @Column(unique = true)
    @Email @NotBlank
    private String email;

    @NotBlank
    private String password;

    @Column(unique = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Role role;

    // private LocalDateTime createdAt;
    private ZonedDateTime createdAt;

    private String token;
    private String refreshToken;

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DonationAppUser() {
        // Set createdAt to the current time in Nairobi (EAT)
        this.createdAt = ZonedDateTime.now(ZoneId.of("Africa/Nairobi"));
  
    }

    public boolean authenticate(String email, String password) throws NoSuchAlgorithmException {
        // Hash the incoming password and compare it with the stored hash
        return passwordEncoder.matches(password, this.password);
    }
    // Custom setter for password
    public DonationAppUser setPassword(String password) throws NoSuchAlgorithmException {
        if (password == null || password.isEmpty()) {
            log.info("Password cannot be empty");
            throw new IllegalArgumentException("Password cannot be empty");
        }
        this.password = passwordEncoder.encode(password); // Hashing with bcrypt
        return this;
    }

    public enum Role {
        ADMIN,
        MINIADMIN,
        DONOR,
        RECIPIENT
    }
}