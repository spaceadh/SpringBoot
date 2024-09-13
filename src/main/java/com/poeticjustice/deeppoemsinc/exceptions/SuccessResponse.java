package com.poeticjustice.deeppoemsinc.exceptions;

import com.poeticjustice.deeppoemsinc.models.DonationAppUser;

import lombok.Data;

@Data
public class SuccessResponse {

    private Integer responseCode;
    private String message;
    private String url;
    private DonationAppUser savedUser;

    public SuccessResponse(Integer responseCode, String message) {
        this.responseCode = responseCode;
        this.message = message;
    }

    public SuccessResponse(Integer responseCode, String message, String url) {
        this.responseCode = responseCode;
        this.message = message;
        this.url = url;
    }

    public SuccessResponse(int responseCode,String message, DonationAppUser savedUser) {
        this.savedUser = savedUser;
        this.message = message;
        this.responseCode = responseCode;
    }

    // Getters and Setters
    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // public DonationAppUser getSavedUser() {
    //     return savedUser;
    // }
    
    // public void setSavedUser(DonationAppUser savedUser) {
    //     this.savedUser = savedUser;
    // }    
}
