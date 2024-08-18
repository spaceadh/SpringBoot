package com.poeticjustice.deeppoemsinc.Repository;

import java.util.List;
import com.poeticjustice.deeppoemsinc.models.Book; // Adjust the import to the new location

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRespository extends JpaRepository<Book, Integer> {

    // Custom query to search Book posts by title or author
    List<Book> findByAuthor(String author);
    List<Book> findByTitleContainingOrAuthorContaining(String text, String textAgain);  
    List<Book> findByUserId(int userId);  
}