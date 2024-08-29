package com.poeticjustice.deeppoemsinc.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.poeticjustice.deeppoemsinc.models.Book;
import com.poeticjustice.deeppoemsinc.models.mysql.User;
import com.poeticjustice.deeppoemsinc.Repository.BookRespository;
import com.poeticjustice.deeppoemsinc.Repository.mysql.UserRespository;
import com.poeticjustice.deeppoemsinc.exceptions.InvalidToken;
import com.poeticjustice.deeppoemsinc.exceptions.LacksAuthorizationHeader;
import com.poeticjustice.deeppoemsinc.exceptions.UnauthorizedUser;
import com.poeticjustice.deeppoemsinc.service.BookService;
import com.poeticjustice.deeppoemsinc.utils.JwtTokenUtil;

import jakarta.validation.constraints.Email;

@Controller
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    UserController userController;
    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    @Autowired
    UserRespository UserRespository;

    @Autowired
    BookRespository bookRespository;

    User loggedInUser;

    
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
            if (email == null || email.isEmpty()) {
                throw new UnauthorizedUser("User is not authorized");
            }   

            // Check if user exists
            List<User> users = UserRespository.findByEmail(email);
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
            // throw e; // Rethrow to propagate the exception or handle it accordingly
            throw new LacksAuthorizationHeader("Authorization header is missing");
        } catch (UnauthorizedUser e) {
            logger.error("Unauthorized user error: ", e);
            // throw e; // Rethrow to propagate the exception or handle it accordingly
            throw new UnauthorizedUser("User is not authorized");
        } catch (InvalidToken e) {
            logger.error("Invalid token error: ", e);
            // throw e; // Rethrow to propagate the exception or handle it accordingly
            throw new InvalidToken("Token is not valid");
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
            throw new RuntimeException("An unexpected error occurred", e); // General catch-all for unexpected exceptions
        }
    }

    private boolean checkToken(User user,String token){
        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
        return jwtTokenUtil.validateToken(token, user);
    }

    @PostMapping("/create")
    public ResponseEntity<String> createBook(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "pdf", required = false) MultipartFile pdfFile,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @RequestParam(value = "userId", required = false) Integer userId,
            @RequestHeader Map<String, String> headers) {
        try {
            // Read authentication token from headers
            String authorization = headers.get("authorization");

            // Check authorization
            if (authorization == null || authorization.isEmpty()) {
                throw new LacksAuthorizationHeader("Authorization header is missing");
                // return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization header is missing");
            }
            
            logger.info("Authorization: {}", authorization);

            logger.info("Request Body - Title: {}", title);
            logger.info("Request Body - Author: {}", author);


            // Load middleware
            middleWare(authorization);

            // Validate input parameters
            if (title == null || title.trim().isEmpty()) {
                // return ResponseEntity.badRequest().body("Title is required");
                throw new LacksAuthorizationHeader("Title is required");
            }
            if (author == null || author.trim().isEmpty()) {
                throw new LacksAuthorizationHeader("Author is required");
                // return ResponseEntity.badRequest().body("Author is required");
            }
        
            // Save files and create book
            String pdfPath = bookService.saveFile(pdfFile, "pdf");
            String imagePath = bookService.saveFile(imageFile, "images");

            // Log the request details
            logger.info("Request Body - Title: {}", title);
            logger.info("Request Body - Author: {}", author);
            logger.info("Request Body - PDF File: {}", pdfPath);
            logger.info("Request Body - image Path: {}", imagePath);
            // logger.info("Request Body - PDF File: {}", pdfFile.getOriginalFilename());

            // Create and save book
            Book book = new Book();

            // Book savedBook = bookRespository.save(new Book(title, author, pdfPath, imagePath, 1));
            Book savedBook = bookRespository.save(new Book(title, author, pdfPath, imagePath, this.loggedInUser.getId()));
            // Book book = new Book(title, author, pdfPath, imagePath, 1);
            logger.info("Book Body: {}", savedBook);
            // bookService.saveBook(savedBook);

            return ResponseEntity.ok("Book created successfully");
        } catch (IOException e) {
            logger.error("Failed to upload files", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload files");
        } catch (LacksAuthorizationHeader e) {
            logger.error("An unexpected error occurred", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }
}