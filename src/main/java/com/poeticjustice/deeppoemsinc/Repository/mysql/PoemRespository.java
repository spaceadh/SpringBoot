package com.poeticjustice.deeppoemsinc.Repository.mysql;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.poeticjustice.deeppoemsinc.models.mysql.Post;



@Repository
public interface PoemRespository extends JpaRepository<Post, Integer> {

    List<Post> findByTitle(String title);
    List<Post> findByContent(String content);
    List<Post> findByAuthorOrTitleOrContentContaining(String author, String title, String content);
}