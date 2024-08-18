package com.poeticjustice.deeppoemsinc.Repository.mongodb;

import com.poeticjustice.deeppoemsinc.models.mongo.PostDocument;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PoemMongoRepository extends MongoRepository<PostDocument, Integer> {
    PostDocument findByTitle(String title);
    PostDocument findByContent(String content);
    PostDocument findByAuthorOrTitleOrContentContaining(String author, String title, String content);   
}