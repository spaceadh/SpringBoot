package com.poeticjustice.deeppoemsinc.presentation.controllers;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Cipher;
// import javax.crypto.BadPaddingException;
// import javax.crypto.IllegalBlockSizeException;
// import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;

@RestController
@RequestMapping("/api/security")
public class SecurityController {

    private static PublicKey pubKey;

    @PostMapping("/generate-checksum")
    public String generateChecksum(@RequestBody Map<String, String> payload) {
        String serviceName = payload.getOrDefault("serviceName", "");
        String initChannelID = payload.getOrDefault("initChannelID", "");
        String requestRefNum = payload.getOrDefault("requestRefNum", "");
        String senderAccountNo = payload.getOrDefault("senderAccountNo", "");
        String tranAmount = payload.getOrDefault("tranAmount", "");
        String tranCCY = payload.getOrDefault("tranCCY", "");

        System.out.println("serviceName     : [" + serviceName + "]");
        System.out.println("initChannelID   : [" + initChannelID + "]");
        System.out.println("requestRefNum   : [" + requestRefNum + "]");
        System.out.println("senderAccountNo : [" + senderAccountNo + "]");
        System.out.println("tranAmount      : [" + tranAmount + "]");
        System.out.println("tranCCY         : [" + tranCCY + "]");

        String pubKeyString = "MIIBITANBgkqhkiG9w0BAQEFAAOCAQ4AMIIBCQKCAQBTxM1lUyghDXUKsuhWUPqiGXsVp0sxt0gtjzjfkNtpqWapepIdwEr6J359EdeYHK/bibNdAVhtfSRADRYcuPuVOnsWGnnM7VVGUF6rNPMMlPJPwszFlm8JiFRbH34ZbhBo80Pa2gaOGkvqqyzxnyqORnxywTmnV4wjsk9ZAAdcXlvRsId4cXuq44BE7RevQ/boudg09N4u9B7vL2u8tQ5BklP2jMCsra8s1ytzf1w/5SnjC2z6hghZTzdVqxPY+ZN82Z2GO3bdZXjegM3ebNLWKMUFjrNolMfcVo7QLuxXRLyX/VoR+t5WQiS9zGhVCY2GqGFr4RLegLx1xL3jpKHZAgMBAAE=";

        String strToEncrypt = serviceName + initChannelID + requestRefNum + senderAccountNo + tranAmount + tranCCY;
        return getChecksum(pubKeyString, strToEncrypt);
    }

    private String getChecksum(String publicKey, String strToEncrypt) {
        try {
            return encryptDataRSA(strToEncrypt, publicKey);
        } catch (Exception e) {
            e.printStackTrace();
            return "NA";
        }
    }

    private String encryptDataRSA(String message, String pubKeyString) throws Exception {
        byte[] encryptedBytes = encryptRSA(message.getBytes(), pubKeyString);
        byte[] base64Bytes = Base64.encodeBase64(encryptedBytes, false);
        String encodedEncryptedBytes = new String(base64Bytes);
        return DigestUtils.sha256Hex(encodedEncryptedBytes);
    }

    private byte[] encryptRSA(byte[] data, String pubKeyString) throws Exception {
        byte[] publicBytes = Base64.decodeBase64(pubKeyString.getBytes());
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        pubKey = keyFactory.generatePublic(keySpec);

        Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        return cipher.doFinal(data);
    }
}
