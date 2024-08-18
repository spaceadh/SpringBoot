package com.poeticjustice.deeppoemsinc.Repository.mysql;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.poeticjustice.deeppoemsinc.models.mysql.User;



@Repository
public interface UserRespository extends JpaRepository<User, Integer> {

    // custom query to search to User post by title or content
    List<User> findByEmail(String email);
    List<User> findByPhoneNumber(String phoneNumber);
    User findById(int id);
    List<User> findByToken(String token);
    List<User> findByFirstNameOrLastNameOrEmailOrPhoneNumberContaining(String firstName, String lastName, String email, String phoneNumber);
}

