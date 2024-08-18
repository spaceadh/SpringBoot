package com.poeticjustice.deeppoemsinc.Repository.mongodb;

import com.poeticjustice.deeppoemsinc.models.mongo.UserDocument;
// import com.poeticjustice.deeppoemsinc.models.mysql.User;

// import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMongoRepository extends MongoRepository<UserDocument, Integer> {
    UserDocument findByEmail(String email);
    UserDocument findById(int id);
    UserDocument findByPhoneNumber(String phoneNumber);
    UserDocument findByToken(String token);
    UserDocument findByFirstNameOrLastNameOrEmailOrPhoneNumberContaining(String firstName, String lastName, String email, String phoneNumber);
}