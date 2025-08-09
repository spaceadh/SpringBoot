package com.poeticjustice.deeppoemsinc.presentation.controllers;

import com.poeticjustice.deeppoemsinc.application.dtos.UploadFileRequestDto;
import com.poeticjustice.deeppoemsinc.common.exceptions.InvalidToken;
import com.poeticjustice.deeppoemsinc.common.exceptions.LacksAuthorizationHeader;
import com.poeticjustice.deeppoemsinc.common.exceptions.UnauthorizedUser;
import com.poeticjustice.deeppoemsinc.domain.service.FileStorageService;
import com.poeticjustice.deeppoemsinc.domain.service.QuotaService;

import com.poeticjustice.deeppoemsinc.domain.service.SubscriptionValidatorService;
import com.poeticjustice.deeppoemsinc.infrastructure.utils.JwtTokenUtil;
import com.poeticjustice.deeppoemsinc.domain.models.mysql.User;
import com.poeticjustice.deeppoemsinc.domain.Repository.mysql.UserRepository;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
public class FileUploadController {
    private final FileStorageService fileStorageService;
    private final QuotaService quotaService;
    private final SubscriptionValidatorService subscriptionValidator;

    private final UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    User loggedInUser;

    public FileUploadController(QuotaService quotaService,
                                FileStorageService fileStorageService,
                                SubscriptionValidatorService subscriptionValidator,
                                UserRepository userRepository) {
        this.quotaService = quotaService;
        this.fileStorageService = fileStorageService;
        this.subscriptionValidator = subscriptionValidator;
        this.userRepository = userRepository;
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<UploadFileRequestDto.UploadFileResponseDto> uploadFile(
            @RequestPart("file") @NotNull(message = "File cannot be null") MultipartFile file,
            @RequestPart("request") UploadFileRequestDto request) {
        try {
            // Validate subscription
            if (!subscriptionValidator.isSubscribed(request.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new UploadFileRequestDto.UploadFileResponseDto(
                                "User not subscribed", null, HttpStatus.FORBIDDEN));
            }

            // Check quota using the specified Redis client
            if (!quotaService.hasEnoughQuota(request.getUserId(), file.getSize(), request.getClient())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new UploadFileRequestDto.UploadFileResponseDto(
                                "Quota exceeded", null, HttpStatus.BAD_REQUEST));
            }

            // Store file
            String storageUrl = fileStorageService.storeFile(file, request.getCategory(), request.getUserId());

            // Update quota
            quotaService.updateQuota(request.getUserId(), file.getSize(), request.getClient());

            // Return success response
            return ResponseEntity.ok(new UploadFileRequestDto.UploadFileResponseDto(
                    "File uploaded successfully", storageUrl, HttpStatus.OK));
        } catch (IllegalArgumentException e) {
            // Handle invalid client type (e.g., not "jedis" or "lettuce")
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new UploadFileRequestDto.UploadFileResponseDto(
                            "Invalid client type: " + e.getMessage(), null, HttpStatus.BAD_REQUEST));
        } catch (Exception e) {
            // Handle other errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new UploadFileRequestDto.UploadFileResponseDto(
                            "Error uploading file: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * Regenerate presigned URL for a private file (1 hour TTL).
     */
    @GetMapping("/regenerate-url/{userId}/{fileName:.+}")
    public ResponseEntity<?> regenerateUrl(@PathVariable String userId, @PathVariable String fileName) {
        try {
            String url = fileStorageService.regeneratePresignedUrl(userId, fileName, 1); // 1 hour
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * List files for a user. If isPublic = true, returns permanent public URLs; otherwise regenerates presigned urls.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> listUserFiles(@PathVariable String userId, @RequestParam(value = "public", defaultValue = "false") boolean isPublic) {
        try {
            List<String> urls = fileStorageService.listUserFiles(userId, isPublic, 1);
            return ResponseEntity.ok(Map.of("files", urls));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }

    private void middleWare(String authorization) {
        try {
            if (authorization == null || authorization.isEmpty()) {
                throw new LacksAuthorizationHeader("Authorization header is missing");
            }
            logger.info("Authorization: {}", authorization);

            String token;

            if (authorization.startsWith("Bearer ")) {
                token = authorization.substring(7).trim(); // Remove 'Bearer ' prefix
            } else {
                token = authorization.trim(); // Use the token as is
            }

            // Initialize JwtTokenUtil
            JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();

            // Extract email from token
            @Email
            String email = jwtTokenUtil.getUsernameFromToken(token);
            logger.info("Email extracted: {}", email);

            if (email == null || email.isEmpty()) {
                throw new UnauthorizedUser("User is not authorized");
            }

            // Check if user exists
            List<User> users = userRepository.findByEmail(email);
            logger.info("Users found: {}", users);
            if (users.isEmpty()) {
                throw new UnauthorizedUser("User is not authorized");
            }
            loggedInUser = users.get(0);

            // Validate the token
            if (!checkToken(loggedInUser, token)) {
                logger.info("Token is not valid");
                throw new InvalidToken("Token is not valid");
            }

            this.loggedInUser = loggedInUser;

        } catch (LacksAuthorizationHeader e) {
            logger.error("Authorization error: ", e);
            throw e;
        } catch (UnauthorizedUser e) {
            logger.error("Unauthorized user error: ", e);
            throw e;
        } catch (InvalidToken e) {
            logger.error("Invalid token error: ", e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
            throw new RuntimeException("An unexpected error occurred", e);
        }
    }

    private boolean checkToken(User user, String token) {
        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
        return jwtTokenUtil.validateToken(token, user);
    }
}