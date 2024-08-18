package com.poeticjustice.deeppoemsinc.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// import com.poeticjustice.deeppoemsinc.models.Post;
import com.poeticjustice.deeppoemsinc.models.mysql.Post;
import com.poeticjustice.deeppoemsinc.models.mysql.User;
import com.poeticjustice.deeppoemsinc.utils.JwtTokenUtil;
import com.poeticjustice.deeppoemsinc.Repository.mysql.UserRespository;
import com.poeticjustice.deeppoemsinc.exceptions.InvalidToken;
import com.poeticjustice.deeppoemsinc.exceptions.LacksAuthorizationHeader;
import com.poeticjustice.deeppoemsinc.exceptions.UnauthorizedUser;
import com.poeticjustice.deeppoemsinc.Repository.mysql.PoemRespository;

import jakarta.persistence.ElementCollection;
import jakarta.validation.constraints.Email;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin
@RestController
public class PoemController {

    @Autowired
    PoemRespository poemRespository;

    @Autowired
    UserRespository UserRespository;

    User loggedInUser;

    private static final Logger logger = LoggerFactory.getLogger(PoemController.class);

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

    @GetMapping("/poem")
    @ElementCollection
    public List<Post> index(){
        return poemRespository.findAll();
    }

    @GetMapping("/poem/{id}")
    public Post show(@PathVariable String id){
        int blogId = Integer.parseInt(id);
        return  poemRespository.findById(blogId).get();
    }

    @PostMapping("/poem/get")
    public Post getBlog(@RequestBody Map<String, String> body){
        int blogId = Integer.parseInt(body.get("id"));
        //log the variable body to the console
        return  poemRespository.findById(blogId).get();
    }

    @PostMapping("/poem/search")
    @ElementCollection
    public List<Post> search(@RequestBody Map<String, String> body){
        String searchTerm = body.get("text");
        return poemRespository.findByAuthorOrTitleOrContentContaining(searchTerm, searchTerm, searchTerm);
    }
    @PostMapping("/poem")
    public Post create(@RequestBody Map<String, String> body, @RequestHeader Map<String, String> headers) {
        String title = body.get("title");
        String content = body.get("content");
        String imageUrl = body.get("imageUrl");
        String status = "pendingApproval";  // Default status
        int likes = 0;  // Default likes
    
        // Read authentication token from headers
        String authorization = headers.get("authorization");
    
        // Check authorization
        if (authorization == null || authorization.isEmpty()) {
            throw new LacksAuthorizationHeader("Authorization header is missing");
        }
    
        logger.info("Authorization: {}", authorization);
    
        // Load middleware
        middleWare(authorization);
    
        // Get the logged-in user's ID
        Integer userId = this.loggedInUser.getId();

        String authorFirstName = this.loggedInUser.getFirstName();
        String authorLastName = this.loggedInUser.getLastName();

        String author = authorFirstName + " " + authorLastName;

        logger.info("Author: {}", author);
    
        // Convert userId to String
        String userIdStr = userId.toString();
    
        // Create and save the new post
        Post newPost = new Post(title, content, author, userIdStr, status, imageUrl, likes);
        return poemRespository.save(newPost);
    }

    @PutMapping("/poem/{id}")
    public Post update(@PathVariable String id, @RequestBody Map<String, String> body){
        int blogId = Integer.parseInt(id);
        // getting poem
        Post poem = poemRespository.findById(blogId).get();
        poem.setTitle(body.get("title"));
        poem.setContent(body.get("content"));
        return poemRespository.save(poem);
    }

    @DeleteMapping("poem/{id}")
    public boolean delete(@PathVariable String id){
        int blogId = Integer.parseInt(id);
        poemRespository.deleteById(blogId);
        return true;
    }

    @PostMapping("/poem/get-all")
    @ElementCollection
    public List<Post> getAll(){
        return poemRespository.findAll();
    }


    @PostMapping("/poem/create")
    public Post createBlog(@RequestBody Map<String, String> body, @RequestHeader Map<String, String> headers) {
        String title = body.get("title");
        String content = body.get("content");
        String imageUrl = body.get("imageUrl");
        String status = "pendingApproval";  // Default status
        int likes = 0;  // Default likes

        // Read authentication token from headers
        String authorization = headers.get("authorization");

        // Check authorization
        if (authorization == null || authorization.isEmpty()) {
            throw new LacksAuthorizationHeader("Authorization header is missing");
        }

        logger.info("Authorization: {}", authorization);

        // Load middleware
        middleWare(authorization);

        // Get the logged-in user's ID
        Integer userId = this.loggedInUser.getId();

        String authorFirstName = this.loggedInUser.getFirstName();
        String authorLastName = this.loggedInUser.getLastName();

        String author = authorFirstName + " " + authorLastName;

        logger.info("Author: {}", author);
    
        // Convert userId to String
        String userIdStr = userId.toString();
    
        // Create and save the new post
        Post newPost = new Post(title, content, author, userIdStr, status, imageUrl, likes);
        return poemRespository.save(newPost);
    }


    @PostMapping("/poem/update")
    public Post updateBlog(@RequestBody Map<String, String> body){
        int blogId = Integer.parseInt(body.get("id"));
        // getting poem
        Post poem = poemRespository.findById(blogId).get();
        poem.setTitle(body.get("title"));
        poem.setContent(body.get("content"));
        return poemRespository.save(poem);
    }

    @PostMapping("poem/delete")
    public boolean deleteBlog(@RequestBody Map<String, String> body){
        int blogId = Integer.parseInt(body.get("id"));
        poemRespository.deleteById(blogId);
        return true;
    }

   
    @PostMapping("/poem/like/{id}")
    public ResponseEntity<String> likeOrDislikePost(@PathVariable String id, @RequestHeader Map<String, String> headers) {
        
        // Read authentication token from headers
        String authorization = headers.get("authorization");

        // Check authorization
        if (authorization == null || authorization.isEmpty()) {
            throw new LacksAuthorizationHeader("Authorization header is missing");
        }

        logger.info("Authorization: {}", authorization);

        // Load middleware
        middleWare(authorization);

        // Read the user ID from the logged-in user (assuming this.loggedInUser is set by middleware)
        // Integer idInt = id.parseInt();
        Integer userId = this.loggedInUser.getId();
        String userIdStr = userId.toString();

        // Parse the post ID
        Integer postId;
        try {
            postId = Integer.valueOf(id);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid post ID");
        }

        // Find the post by ID
        Optional<Post> optionalPost = poemRespository.findById(postId);

        if (!optionalPost.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }

        Post post = optionalPost.get();

        // Check if the user has already liked the post
        if (post.getLikedBy().contains(userIdStr)) {
            // User has already liked the post, so remove the like (dislike)
            post.getLikedBy().remove(userIdStr);
            post.setLikes(post.getLikes() - 1);
            poemRespository.save(post);
            return ResponseEntity.ok("Post disliked");
        } else {
            // User has not liked the post, so add the like
            post.getLikedBy().add(userIdStr);
            post.setLikes(post.getLikes() + 1);
            poemRespository.save(post);
            return ResponseEntity.ok("Post liked");
        }
    }
}