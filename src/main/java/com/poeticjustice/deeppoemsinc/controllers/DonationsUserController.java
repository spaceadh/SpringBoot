package com.poeticjustice.deeppoemsinc.controllers;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.context.Context;

import com.poeticjustice.deeppoemsinc.Repository.DonationUserRespository;
import com.poeticjustice.deeppoemsinc.exceptions.EmailAlreadyInUseException;
import com.poeticjustice.deeppoemsinc.exceptions.ErrorResponse;
import com.poeticjustice.deeppoemsinc.exceptions.SuccessResponse;
import com.poeticjustice.deeppoemsinc.exceptions.InvalidEmailException;
import com.poeticjustice.deeppoemsinc.exceptions.InvalidPasswordException;
import com.poeticjustice.deeppoemsinc.exceptions.InvalidPhoneNumberException;
import com.poeticjustice.deeppoemsinc.exceptions.InvalidToken;
import com.poeticjustice.deeppoemsinc.exceptions.LacksAuthorizationHeader;
import com.poeticjustice.deeppoemsinc.exceptions.PhoneNumberAlreadyInUseException;
import com.poeticjustice.deeppoemsinc.exceptions.UnauthorizedUser;
import com.poeticjustice.deeppoemsinc.models.DonationAppUser;
import com.poeticjustice.deeppoemsinc.utils.EmailService;
import com.poeticjustice.deeppoemsinc.utils.HtmlService;
import com.poeticjustice.deeppoemsinc.utils.JwtTokenUtil;
import com.poeticjustice.deeppoemsinc.utils.PdfService;
import com.poeticjustice.deeppoemsinc.validators.LoginRequest;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.validation.Valid;

@RestController
@CrossOrigin
@RequestMapping("/donationuser")
public class DonationsUserController {
    
    private static final Logger logger = LoggerFactory.getLogger(DonationsUserController.class);

    @Autowired
    private DonationUserRespository UserRespository;
    
    @Autowired
    private EmailService emailService;

    DonationAppUser loggedInUser;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotDonorPassword(@RequestBody Map<String, String> body) {
        // Log the request body and token
        logger.info("Request Body: {}", body);

        String email = body.get("email");

        List<DonationAppUser> users = UserRespository.findByEmail(email);
        if (users == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(404, "No user found with this email address"));
        }

        DonationAppUser user = users.get(0);
        // Generate a refresh token
        String refreshToken = UUID.randomUUID().toString();
        user.setRefreshToken(refreshToken);
        UserRespository.save(user);

        // Prepare context for Thymeleaf
        Context context = new Context();

        Dotenv dotenv = Dotenv.load();
        String baseUrl = dotenv.get("BASE_URL");
        logger.info("Database URL: {}", baseUrl);

        // Send email
        String resetUrl = baseUrl +"/reset-password?token=" + refreshToken;
        
        context.setVariable("resetUrl", resetUrl);
         // Send welcome email
        try {
            emailService.sendHtmlMessage(email, "Forget Password", "email-templates/forgot-password", context);
        } catch (MessagingException | jakarta.mail.MessagingException e) {
            ResponseEntity.badRequest().body(new ErrorResponse(404, "Error sending an email"));
        }

        // return ResponseEntity.ok(new SuccessResponse("Reset email sent"));
        // return ResponseEntity.status(HttpStatus.CREATED).body("Reset email sent");
        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponse(200, "Reset email sent"));
    }

    @PostMapping("/admin-register")
    public ResponseEntity<?> registerAdmin(@RequestBody Map<String, String> body) throws NoSuchAlgorithmException{
         // Log the request body and token
        logger.info("Request Body: {}", body);

        String firstName = body.get("firstName");
        String lastName = body.get("lastName");
        String email = body.get("email");
        //validate email    
        Pattern p = Pattern.compile("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
        Matcher m = p.matcher(email);
        if(!m.matches()){
            // throw new InvalidEmailException("Email is not valid");
            return ResponseEntity.badRequest().body(new ErrorResponse(404, "Email is not valid"));
        }
      
        //check if email is in use
        List<DonationAppUser> users = UserRespository.findByEmail(email);
        if(!users.isEmpty()){
            // throw new EmailAlreadyInUseException("Email is already in use");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(404, "Email is already in use"));
        }
        String phoneNumber = body.get("phoneNumber");
        //validate phone
        p = Pattern.compile("^[0-9]{10}$");
        m = p.matcher(phoneNumber);
        if(!m.matches()){
            // throw new InvalidPhoneNumberException("Phone Number is not valid");
            return ResponseEntity.badRequest().body(new ErrorResponse(404, "Phone Number is not valid"));
        }
        //check if phone is in use
        users = UserRespository.findByPhoneNumber(phoneNumber);
        if(!users.isEmpty()){
            // throw new PhoneNumberAlreadyInUseException("Phone Number is already in use");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(404, "Phone Number is already in use"));
        }
        String password = body.get("password");
        //validate password
        p = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");
        m = p.matcher(password);
        if(!m.matches()){
            // throw new InvalidPasswordException("Password is not valid. Must contain at least 8 characters, one uppercase, one lowercase, one number, and one special character");
            return ResponseEntity.badRequest().body(new ErrorResponse(404, "Password is not valid. Must contain at least 8 characters, one uppercase, one lowercase, one number, and one special character"));
        }
        
        // Create and save the user
        DonationAppUser user = new DonationAppUser();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setPassword(password); // This will call the custom setter
        user.setRole(DonationAppUser.Role.ADMIN);
 
        DonationAppUser savedUser = UserRespository.save(user);
            
        logger.info("User saved: {}", savedUser);
        // return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponse(200, savedUser));
        return ResponseEntity.status(HttpStatus.CREATED)
                     .body(new SuccessResponse(200,"Admin has been created Successfully.", savedUser));
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetDonorPassword(@RequestParam String token, @RequestBody Map<String, String> body) throws NoSuchAlgorithmException {
        // Log the request body and token
        logger.info("Request Body: {}", body);

        String newPassword = body.get("newPassword");

        List<DonationAppUser> users = UserRespository.findByRefreshToken(token);
        if (users == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(404, "No user found with this email address"));
        }

        DonationAppUser user = users.get(0);
        // Validate new password
        if (!isValidPassword(newPassword)) {
            return ResponseEntity.badRequest().body(new ErrorResponse(404, "Password does not meet the criteria"));
        }

        // Update user password
        user.setPassword(newPassword);
        UserRespository.save(user);

        // Clear the refresh token
        user.setRefreshToken(null);

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponse(200,"Password updated successfully"));
    }

    public void middleWare(String authorization) {
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
            String email = jwtTokenUtil.getUsernameFromToken(token);
            logger.info("Email {} :", email);
            
            if (email == null || email.isEmpty()) {
                throw new UnauthorizedUser("User is not authorized");
            }

            // Check if user exists
            List<DonationAppUser> users = UserRespository.findByEmail(email);
            if (users.isEmpty()) {
                throw new UnauthorizedUser("User is not authorized");
            }
            DonationAppUser loggedInUser = users.get(0);

            // Validate the token
            if (!checkToken(loggedInUser, token)) {
                logger.info("Token is not valid");
                throw new InvalidToken("Token is not valid");
            }

            this.loggedInUser = loggedInUser;

        } catch (LacksAuthorizationHeader e) {
            logger.error("Authorization error: ", e);
            throw e; // Rethrow to propagate the exception or handle it accordingly
        } catch (UnauthorizedUser e) {
            logger.error("Unauthorized user error: ", e);
            throw e; // Rethrow to propagate the exception or handle it accordingly
        } catch (InvalidToken e) {
            logger.error("Invalid token error: ", e);
            throw e; // Rethrow to propagate the exception or handle it accordingly
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
            throw new RuntimeException("An unexpected error occurred", e); // General catch-all for unexpected exceptions
        }
    }

    private boolean checkToken(DonationAppUser user,String token){
        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
        return jwtTokenUtil.validateDonationToken(token, user);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createDonorUser(@RequestBody Map<String, String> body, @RequestHeader Map<String, String> headers) {
        try {
            // Read authentication token from headers
            String authorization = headers.get("authorization");
            if (authorization == null) {
                throw new LacksAuthorizationHeader("Authorization header is missing");
            }

            // Load middleware
            middleWare(authorization);

            // Extract and validate user details from the request body
            String firstName = body.get("firstName");
            String lastName = body.get("lastName");
            String email = body.get("email");
            String phoneNumber = body.get("phoneNumber");
            String password = body.get("password");

            // Validate email
            if (!isValidEmail(email)) {
                throw new InvalidEmailException("Email is not valid");
            }

            // Check if email is in use
            if (isEmailInUse(email)) {
                throw new EmailAlreadyInUseException("Email is already in use");
            }

            // Validate phone number
            if (!isValidPhoneNumber(phoneNumber)) {
                throw new InvalidPhoneNumberException("Phone Number is not valid");
            }

            // Check if phone number is in use
            if (isPhoneNumberInUse(phoneNumber)) {
                throw new PhoneNumberAlreadyInUseException("Phone Number is already in use");
            }

            // Validate password
            if (!isValidPassword(password)) {
                throw new InvalidPasswordException("Password is not valid. Must contain at least 8 characters, one uppercase, one lowercase, one number, and one special character");
            }

            // Create and save the user
            DonationAppUser user = new DonationAppUser();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setPhoneNumber(phoneNumber);
            user.setPassword(password); // This will call the custom setter
            user.setRole(DonationAppUser.Role.MINIADMIN);
            DonationAppUser savedUser = UserRespository.save(user);

            // Prepare context for Thymeleaf
            Context context = new Context();
            context.setVariable("firstName", firstName);
            context.setVariable("lastName", lastName);
            context.setVariable("email", email);

            // Send welcome email
            emailService.sendHtmlMessage(email, "Welcome to our service", "email-templates/welcome-email", context);

            // return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
            return ResponseEntity.status(HttpStatus.CREATED)
                     .body(new SuccessResponse(200,"User Has been created successfully.", savedUser));

        } catch (LacksAuthorizationHeader | UnauthorizedUser | InvalidToken e) {
            logger.error("Authorization error: ", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(404, e.getMessage()));
        } catch (InvalidEmailException | EmailAlreadyInUseException | InvalidPhoneNumberException | PhoneNumberAlreadyInUseException | InvalidPasswordException e) {
            logger.error("Validation error: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(404, e.getMessage()));
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error creating user: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(404, "An error occurred"));
        } catch (MessagingException e) {
            logger.error("Error sending email: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(404, "An error occurred while sending email"));
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(404, "An unexpected error occurred"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> logindonor(@Valid @RequestBody LoginRequest loginRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
        // Collect all validation errors
            List<String> errors = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(new ErrorResponse(404, String.join(", ", errors)));
        }
        
        String email = loginRequest.getEmail();
        
        // Validate email
        if (!isValidEmail(email)) {
            throw new InvalidEmailException("Email is not valid");
        }
        String password = loginRequest.getPassword();
        List<DonationAppUser> users = UserRespository.findByEmail(email);

        if (users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(404, "Email is not valid"));
        }

        DonationAppUser user = users.get(0);
        try {
            if (user.authenticate(email, password)) {
                JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
                String token = jwtTokenUtil.generateDonationToken(user);
                user.setToken(token);
                UserRespository.save(user);
                // return ResponseEntity.ok(user);
                return ResponseEntity.status(HttpStatus.CREATED)
                     .body(new SuccessResponse(200,"User has been logged In Successfully." ,user));

            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(404, "Password is incorrect"));
            }
        } catch (Exception e) {
            logger.error("Error logging user: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(404, "An error occurred"));
        }
    }

    private boolean isValidEmail(String email) {
        Pattern p = Pattern.compile("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
        Matcher m = p.matcher(email);
        return m.matches();
    }

    private boolean isEmailInUse(String email) {
        List<DonationAppUser> users = UserRespository.findByEmail(email);
        return !users.isEmpty();
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        Pattern p = Pattern.compile("^[0-9]{10}$");
        Matcher m = p.matcher(phoneNumber);
        return m.matches();
    }

    private boolean isPhoneNumberInUse(String phoneNumber) {
        List<DonationAppUser> users = UserRespository.findByPhoneNumber(phoneNumber);
        return !users.isEmpty();
    }

    private boolean isValidPassword(String password) {
        Pattern p = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");
        Matcher m = p.matcher(password);
        return m.matches();
    }
}