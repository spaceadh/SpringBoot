package com.poeticjustice.deeppoemsinc.controllers;

import java.io.IOException;

// import org.assertj.core.internal.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
// import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.FileOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.thymeleaf.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
// import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.scheduling.annotation.Scheduled;

import com.poeticjustice.deeppoemsinc.Repository.mysql.UserRespository;
import com.poeticjustice.deeppoemsinc.exceptions.EmailAlreadyInUseException;
import com.poeticjustice.deeppoemsinc.exceptions.IncorrectPasswordException;
import com.poeticjustice.deeppoemsinc.exceptions.ErrorResponse;
import com.poeticjustice.deeppoemsinc.exceptions.InvalidEmailException;
import com.poeticjustice.deeppoemsinc.exceptions.InvalidPasswordException;
import com.poeticjustice.deeppoemsinc.exceptions.InvalidPhoneNumberException;
import com.poeticjustice.deeppoemsinc.exceptions.InvalidToken;
import com.poeticjustice.deeppoemsinc.exceptions.LacksAuthorizationHeader;
import com.poeticjustice.deeppoemsinc.exceptions.PhoneNumberAlreadyInUseException;
import com.poeticjustice.deeppoemsinc.exceptions.UnauthorizedUser;
import com.poeticjustice.deeppoemsinc.models.mysql.User;

import jakarta.validation.constraints.Email;

import org.springframework.validation.BindingResult;

import com.poeticjustice.deeppoemsinc.validators.LoginRequest;
import com.poeticjustice.deeppoemsinc.utils.EmailService;
import com.poeticjustice.deeppoemsinc.utils.HtmlService;
import com.poeticjustice.deeppoemsinc.utils.JwtTokenUtil;
import com.poeticjustice.deeppoemsinc.utils.PdfService;
// import org.springframework.http.ContentDisposition;

import jakarta.persistence.ElementCollection;

import jakarta.validation.Valid;

@CrossOrigin
@RestController
public class UserController {

    @Autowired
    UserRespository UserRespository;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private HtmlService htmlService;

    @Autowired
    private PdfService pdfService;


    User loggedInUser;

    @PostMapping("/user/generate")
    public ResponseEntity<Map<String, String>> generatePdf(@RequestBody Map<String, String> body,@RequestHeader Map<String, String> headers) throws IOException {
        // Read authentication token from headers
        String authorization = headers.get("authorization");
       
        // Load middleware
        middleWare(authorization);

        // Retrieve all users from the repository
        List<User> users = UserRespository.findAll();

        // Prepare context for Thymeleaf with user data
        Context context = new Context();
        context.setVariable("users", users);

        // Generate HTML from the template
        String html = htmlService.generateHtml("email-templates/user-table", context);
        
        // Convert HTML to PDF
        byte[] pdfBytes = pdfService.convertHtmlToPdf(html);
        
        // Define the file path and name
        String fileName = "user-data.pdf";
        Path uploadsDir = Paths.get("uploads").toAbsolutePath();

        if (!Files.exists(uploadsDir)) {
            Files.createDirectory(uploadsDir);
        }
        Path filePath = uploadsDir.resolve(fileName);

        // // Set HTTP headers for PDF response
        // HttpHeaders responseHeaders = new HttpHeaders();
        // responseHeaders.setContentType(MediaType.APPLICATION_PDF);
        // ContentDisposition contentDisposition = ContentDisposition.attachment()
        //     .filename("user-data.pdf")
        //     .build();
        // responseHeaders.setContentDisposition(contentDisposition);
         // Save the PDF to a file
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(pdfBytes);
        }

        // Construct the URL for accessing the saved PDF
        String pdfUrl = "/uploads/" + fileName;

        // Respond with the URL of the saved PDF
        return new ResponseEntity<>(Map.of("pdfUrl", pdfUrl), HttpStatus.OK);
    }

    @PostMapping("/user/sendGeneratePdf")
    public ResponseEntity<Map<String, String>> sendGeneratePdf(@RequestBody Map<String, String> body, @RequestHeader Map<String, String> headers) throws IOException, MessagingException, jakarta.mail.MessagingException {
        // Read authentication token from headers
        String authorization = headers.get("authorization");
        String email = body.get("email");

        if(email == null || email.isEmpty()){
            throw new EmailAlreadyInUseException("Email is missing from the request body");
        }
        
        // Load middleware
        middleWare(authorization);

        // Retrieve all users from the repository
        List<User> users = UserRespository.findAll();

        // Prepare context for Thymeleaf with user data
        Context context = new Context();
        context.setVariable("users", users);

        // Generate HTML from the template
        String html = htmlService.generateHtml("email-templates/user-table", context);
        
        // Convert HTML to PDF
        byte[] pdfBytes = pdfService.convertHtmlToPdf(html);
        
        // Define the file path and name
        String fileName = "user-data.pdf";
        Path uploadsDir = Paths.get("uploads").toAbsolutePath();
        if (!Files.exists(uploadsDir)) {
            Files.createDirectory(uploadsDir);
        }
        Path filePath = uploadsDir.resolve(fileName);

        // Save the PDF to a file
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(pdfBytes);
        }

        // Send email with PDF attachment
        emailService.sendHtmlMessageWithAttachment(email, "User Data", "email-templates/user-email", context, filePath.toFile());

        // Delete the PDF file
        Files.delete(filePath);

        // Construct the URL for accessing the saved PDF
        String pdfUrl = "/uploads/" + fileName;

        // Respond with the URL of the saved PDF
        return new ResponseEntity<>(Map.of("pdfUrl", pdfUrl), HttpStatus.OK);
    }

    // 0 0 0 * * ? – Runs at midnight every day.
    // 0 0 8 * * ? – Runs at 8 AM every day.
    // 0 0/5 * * * ? – Runs every 5 minutes.
    // 0 0/1 * * * ? – Runs every 1 minutes.
    
    // @Scheduled(cron = "0 0/1 * * * ?") // Runs every day at midnight
    public void ScheduledgenerateAndSendPdf() throws IOException, MessagingException, jakarta.mail.MessagingException {
        // Retrieve all users from the repository
        // List<User> users = userRepository.findAll();
        List<User> users = UserRespository.findAll();

        // Prepare context for Thymeleaf with user data
        Context context = new Context();
        context.setVariable("users", users);

        // Generate HTML from the template
        String html = htmlService.generateHtml("email-templates/user-table", context);

        // Convert HTML to PDF
        byte[] pdfBytes = pdfService.convertHtmlToPdf(html);

        // Define the file path and name
        String fileName = "user-data.pdf";
        Path uploadsDir = Paths.get("uploads").toAbsolutePath();
        if (!Files.exists(uploadsDir)) {
            Files.createDirectory(uploadsDir);
        }
        Path filePath = uploadsDir.resolve(fileName);

        // Save the PDF to a file
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(pdfBytes);
        }

        String email = "alvinvictor023@gmail.com";

        // Send email with PDF attachment
        emailService.sendHtmlMessageWithAttachment(email, "User Data", "email-templates/user-email", context, filePath.toFile());

        // Delete the PDF file
        Files.delete(filePath);
    }

    @PostMapping("/user/get")
    public User getUser(@RequestBody Map<String, String> body,@RequestHeader Map<String, String> headers){

        //read authentication token from headers
        String authorization = headers.get("authorization");
        
        //load middleware
        middleWare(authorization);

        
        int UserId = Integer.parseInt(body.get("id"));
        //log the variable body to the console
        System.out.println(body);
        return  UserRespository.findById(UserId);
    }

    @PostMapping("/user/search")
    @ElementCollection
    public List<User> search(@RequestBody Map<String, String> body,@RequestHeader Map<String, String> headers){

        //read authentication token from headers
        String authorization = headers.get("authorization");
        
        //load middleware
        middleWare(authorization);

        
        String searchTerm = body.get("text");
        return UserRespository.findByFirstNameOrLastNameOrEmailOrPhoneNumberContaining(searchTerm, searchTerm, searchTerm, searchTerm);
    }

    @PostMapping("/user/get-all")
    @ElementCollection
    public List<User> getAll(@RequestHeader Map<String, String> headers){

        //read authentication token from headers
        String authorization = headers.get("authorization");
        
        //load middleware
        middleWare(authorization);

        
        return UserRespository.findAll();
    }

    // @PostMapping("/admin-register")
    // public ResponseEntity<?> registerUser(@RequestBody Map<String, String> body) throws NoSuchAlgorithmException{
    //      // Log the request body and token
    //     logger.info("Request Body: {}", body);

    //     String firstName = body.get("firstName");
    //     String lastName = body.get("lastName");
    //     String email = body.get("email");
    //     //validate email    
    //     Pattern p = Pattern.compile("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
    //     Matcher m = p.matcher(email);
    //     if(!m.matches()){
    //         // throw new InvalidEmailException("Email is not valid");
    //         return ResponseEntity.badRequest().body(new ErrorResponse(404, "Email is not valid"));
    //     }
    //     String role;
    //     role = "Admin";   
    //     //check if email is in use
    //     List<User> users = UserRespository.findByEmail(email);
    //     if(!users.isEmpty()){
    //         // throw new EmailAlreadyInUseException("Email is already in use");
    //         return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(404, "Email is already in use"));
    //     }
    //     String phoneNumber = body.get("phoneNumber");
    //     //validate phone
    //     p = Pattern.compile("^[0-9]{10}$");
    //     m = p.matcher(phoneNumber);
    //     if(!m.matches()){
    //         // throw new InvalidPhoneNumberException("Phone Number is not valid");
    //         return ResponseEntity.badRequest().body(new ErrorResponse(404, "Phone Number is not valid"));
    //     }
    //     //check if phone is in use
    //     users = UserRespository.findByPhoneNumber(phoneNumber);
    //     if(!users.isEmpty()){
    //         // throw new PhoneNumberAlreadyInUseException("Phone Number is already in use");
    //         return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(404, "Phone Number is already in use"));
    //     }
    //     String password = body.get("password");
    //     //validate password
    //     p = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");
    //     m = p.matcher(password);
    //     if(!m.matches()){
    //         // throw new InvalidPasswordException("Password is not valid. Must contain at least 8 characters, one uppercase, one lowercase, one number, and one special character");
    //         return ResponseEntity.badRequest().body(new ErrorResponse(404, "Password is not valid. Must contain at least 8 characters, one uppercase, one lowercase, one number, and one special character"));
    //     }
    //     User user = new User();
    //     // User savedUser = UserRespository.save(user);
    //     User savedUser = UserRespository.save(new User(firstName, lastName, email, phoneNumber, password, role));
    //     return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    // }

    @PostMapping("/user/create")
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> body, @RequestHeader Map<String, String> headers) {
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
            User user = new User(firstName, lastName, email, phoneNumber, password, "Normal");
            User savedUser = UserRespository.save(user);

            // Prepare context for Thymeleaf
            Context context = new Context();
            context.setVariable("firstName", firstName);
            context.setVariable("lastName", lastName);
            context.setVariable("email", email);

            // Send welcome email
            emailService.sendHtmlMessage(email, "Welcome to our service", "email-templates/welcome-email", context);

            return ResponseEntity.ok(savedUser);

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

    private boolean isValidEmail(String email) {
        Pattern p = Pattern.compile("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
        Matcher m = p.matcher(email);
        return m.matches();
    }

    private boolean isEmailInUse(String email) {
        List<User> users = UserRespository.findByEmail(email);
        return !users.isEmpty();
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        Pattern p = Pattern.compile("^[0-9]{10}$");
        Matcher m = p.matcher(phoneNumber);
        return m.matches();
    }

    private boolean isPhoneNumberInUse(String phoneNumber) {
        List<User> users = UserRespository.findByPhoneNumber(phoneNumber);
        return !users.isEmpty();
    }

    private boolean isValidPassword(String password) {
        Pattern p = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");
        Matcher m = p.matcher(password);
        return m.matches();
    }

    private boolean checkToken(User user,String token){
        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
        return jwtTokenUtil.validateToken(token, user);
    }
  
    @PostMapping("/user/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
        // Collect all validation errors
            List<String> errors = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(new ErrorResponse(404, String.join(", ", errors)));
        }

        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();
        List<User> users = UserRespository.findByEmail(email);
        // List<User> users = userRepository.findByEmail(email);

        if (users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(404, "Email is not valid"));
        }

        User user = users.get(0);
        try {
            if (user.authenticate(email, password)) {
                JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
                String token = jwtTokenUtil.generateToken(user);
                user.setToken(token);
                UserRespository.save(user);
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(404, "Password is incorrect"));
            }
        } catch (Exception e) {
            logger.error("Error logging user: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(404, "An error occurred"));
        }
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
            if (email == null || email.isEmpty()) {
                throw new UnauthorizedUser("User is not authorized");
            }

            // Check if user exists
            List<User> users = UserRespository.findByEmail(email);
            if (users.isEmpty()) {
                throw new UnauthorizedUser("User is not authorized");
            }
            User loggedInUser = users.get(0);

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
    
    @PostMapping("/user/update")
    public User updateUser(@RequestBody Map<String, String> body,@RequestHeader Map<String, String> headers){

        String authorization = headers.get("authorization");
        if(authorization == null){
            throw new LacksAuthorizationHeader("Authorization header is missing");
        }
        
        //load middleware
        middleWare(authorization);

        int UserId = Integer.parseInt(body.get("id"));
        // getting User
        User User = UserRespository.findById(UserId).get();
        if(!"".equals(body.get("firstName")) && body.get("firstName")!=null){
            User.setFirstName(body.get("firstName"));
        }
        if(!"".equals(body.get("lastName")) && body.get("lastName")!=null){
            User.setLastName(body.get("lastName"));
        }
        if(!"".equals(body.get("email")) && body.get("email")!=null){
            String email = body.get("email");
            Pattern p = Pattern.compile("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
            Matcher m = p.matcher(email);
            if(!m.matches()){
                throw new InvalidEmailException("Email is not valid");
            }
            User.setEmail(email);
        }
        if(!"".equals(body.get("phoneNumber")) && body.get("phoneNumber")!=null){
            String phoneNumber = body.get("phoneNumber");
            Pattern p = Pattern.compile("^[0-9]{10}$");
            Matcher m = p.matcher(phoneNumber);
            if(!m.matches()){
                throw new InvalidPhoneNumberException("Phone Number is not valid");
            }
            User.setPhoneNumber(body.get("phoneNumber"));
        }

        if(!"".equals(body.get("password")) && body.get("password")!=null){
            try {
                User.setPassword(body.get("password"));
            } catch (NoSuchAlgorithmException e) {
                logger.error("Error updating user: ", e);
            }
        }
        
        return UserRespository.save(User);
    }

    @PostMapping("/user/delete")
    public boolean deleteUser(@RequestBody Map<String, String> body,@RequestHeader Map<String, String> headers){

        String authorization = headers.get("authorization");
        if(authorization == null){
            throw new LacksAuthorizationHeader("Authorization header is missing");
        }
        
        //load middleware
        middleWare(authorization);


        int UserId = Integer.parseInt(body.get("id"));
        UserRespository.deleteById(UserId);
        return true;
    }

}