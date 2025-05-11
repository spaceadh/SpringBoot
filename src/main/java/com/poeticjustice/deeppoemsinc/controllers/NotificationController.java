package com.poeticjustice.deeppoemsinc.controllers;

import com.poeticjustice.deeppoemsinc.dtos.ErrorResponseDto;
import com.poeticjustice.deeppoemsinc.dtos.NotificationRequest;
import com.poeticjustice.deeppoemsinc.dtos.SuccessResponseDto;
import com.poeticjustice.deeppoemsinc.exceptions.InvalidNotificationException;
import com.poeticjustice.deeppoemsinc.exceptions.LacksAuthorizationHeader;
import com.poeticjustice.deeppoemsinc.exceptions.UnauthorizedUser;
import com.poeticjustice.deeppoemsinc.exceptions.InvalidToken;
import com.poeticjustice.deeppoemsinc.models.mysql.User;
import com.poeticjustice.deeppoemsinc.Repository.mysql.UserRespository;
import com.poeticjustice.deeppoemsinc.service.NotificationService;
import com.poeticjustice.deeppoemsinc.utils.JwtTokenUtil;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    private final NotificationService notificationService;
    private final UserRespository userRepository;
    User loggedInUser;

    public NotificationController(NotificationService notificationService, UserRespository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
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

    @PostMapping
    public ResponseEntity<?> createNotification(
            @Valid @RequestBody NotificationRequest request,
            @RequestHeader Map<String, String> headers) {
        try {
            // Read authentication token from headers
            String authorization = headers.get("authorization");
            // Check authorization
            if (authorization == null || authorization.isEmpty()) {
                throw new LacksAuthorizationHeader("Authorization header is missing");
            }

            String acceptLanguage = headers.getOrDefault("accept-language", "en");
            Locale locale = Locale.forLanguageTag(acceptLanguage.split(",")[0]);

            logger.info("Authorization: {}", authorization);
            logger.info("Accept-Language: {}", acceptLanguage);
            // logger.info("Request Body: {}", request);

            // Load middleware
            // middleWare(authorization);

            // Save notification
            Dotenv dotenv = Dotenv.load();
            String baseUrl = dotenv.get("BASE_URL");
            logger.info("Base URL: {}", baseUrl);

            notificationService.saveNotification(request, locale);

            // Return success response
            String notificationUrl = baseUrl + "/notifications/" + request.getReference();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new SuccessResponseDto(201, "Notification created successfully", notificationUrl));

        } catch (LacksAuthorizationHeader | UnauthorizedUser | InvalidToken e) {
            logger.error("Authorization error: ", e);
            String errorCode = "401";
            String errorMessage = notificationService.getLocalizedErrorMessage(errorCode, Locale.forLanguageTag(headers.getOrDefault("accept-language", "en")));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponseDto(401, errorCode, errorMessage));
        } catch (InvalidNotificationException e) {
            logger.error("Validation error: {} - {}", e.getErrorCode(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponseDto(400, e.getErrorCode(), e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
            String errorCode = "500";
            String errorMessage = notificationService.getLocalizedErrorMessage(errorCode, Locale.forLanguageTag(headers.getOrDefault("accept-language", "en")));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto(500, errorCode, errorMessage));
        }
    }
}