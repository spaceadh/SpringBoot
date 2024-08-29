package com.poeticjustice.deeppoemsinc.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.poeticjustice.deeppoemsinc.models.DonationAppUser;



@Repository
public interface DonationUserRespository extends JpaRepository<DonationAppUser, Integer> {

    // custom query to search to DonationAppUser post by title or content
    List<DonationAppUser> findByEmail(String email);
    List<DonationAppUser> findByPhoneNumber(String phoneNumber);
    // DonationAppUser findById(int id);
    List<DonationAppUser> findByToken(String token);
    List<DonationAppUser> findByRefreshToken(String refreshToken);
    // List<DonationAppUser> findByFirstNameOrLastNameOrEmailOrPhoneNumberContaining(String firstName, String lastName, String email, String phoneNumber);
    // void delete(String token);

    // Object findByPhoneNumber(String phoneNumber);
}

