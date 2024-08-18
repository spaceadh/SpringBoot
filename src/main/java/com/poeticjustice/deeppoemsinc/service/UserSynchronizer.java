package com.poeticjustice.deeppoemsinc.service;

import com.poeticjustice.deeppoemsinc.models.mysql.Post;
import com.poeticjustice.deeppoemsinc.models.mysql.User;
import com.poeticjustice.deeppoemsinc.models.mongo.PostDocument;
import com.poeticjustice.deeppoemsinc.models.mongo.UserDocument;
import com.poeticjustice.deeppoemsinc.Repository.mysql.UserRespository;
import com.poeticjustice.deeppoemsinc.Repository.mysql.PoemRespository;
import com.poeticjustice.deeppoemsinc.Repository.mongodb.PoemMongoRepository;
import com.poeticjustice.deeppoemsinc.Repository.mongodb.UserMongoRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserSynchronizer {

    private final UserRespository userRepository;
    private final UserMongoRepository userMongoRepository;

    private final PoemRespository poemRepository;
    private final PoemMongoRepository poemMongoRepository;

    public UserSynchronizer(UserRespository userRepository, UserMongoRepository userMongoRepository, PoemRespository poemRepository, PoemMongoRepository poemMongoRepository) {
        this.userRepository = userRepository;
        this.userMongoRepository = userMongoRepository;
        this.poemRepository = poemRepository;
        this.poemMongoRepository = poemMongoRepository;
    }

    @Scheduled(cron = "0 * * * * ?") // Run every minute
    public void synchronizeUsers() {
        // Fetch users from MySQL
        List<User> mysqlUsers = userRepository.findAll();
        List<Post> mysqlPoems = poemRepository.findAll();
        
        // Sync to MongoDB
        for (User mysqlUser : mysqlUsers) {
            UserDocument userDocument = convertToMongoUser(mysqlUser);
            userMongoRepository.save(userDocument);
        }

        for (Post mysqlPoem : mysqlPoems) {
            PostDocument poemDocument = convertToMongoPost(mysqlPoem);
            poemMongoRepository.save(poemDocument);
        }
    }

    private UserDocument convertToMongoUser(User user) {
        UserDocument userDocument = new UserDocument();
        userDocument.setId(user.getId());
        userDocument.setEmail(user.getEmail());
        userDocument.setFirstName(user.getFirstName());
        userDocument.setLastName(user.getLastName());
        userDocument.setPhoneNumber(user.getPhoneNumber());
        userDocument.setRole(user.getRole());
        userDocument.setPassword(user.getPassword());
        return userDocument;
    }

    private PostDocument convertToMongoPost(Post post) {
        PostDocument postDocument = new PostDocument();
        postDocument.setId(post.getId());
        postDocument.setTitle(post.getTitle());
        postDocument.setContent(post.getContent());
        postDocument.setUserId(post.getUserId());
        postDocument.setAuthor(post.getAuthor());
        postDocument.setStatus(post.getStatus());
        postDocument.setImageUrl(post.getImageUrl());
        postDocument.setLikedBy(post.getLikedBy());
        postDocument.setLikes(post.getLikes());
        return postDocument;
    }
}
