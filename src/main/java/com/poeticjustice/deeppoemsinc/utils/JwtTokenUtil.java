package com.poeticjustice.deeppoemsinc.utils;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.poeticjustice.deeppoemsinc.exceptions.InvalidJwtTokenException;
import com.poeticjustice.deeppoemsinc.exceptions.JwtTokenCreationException;
import com.poeticjustice.deeppoemsinc.models.DonationAppUser;
import com.poeticjustice.deeppoemsinc.models.mysql.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

@Component
public class JwtTokenUtil implements Serializable {

	private static final long serialVersionUID = -2550185165626007488L;

	public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;


	private final String secret = "poeticjustice";

	// Retrieve username from JWT token
    public String getUsernameFromToken(String token) {
        try {
            return getClaimFromToken(token, Claims::getSubject);
        } catch (Exception e) {
            throw new InvalidJwtTokenException("Error getting username from JWT token");
        }
    }

	// Retrieve expiration date from JWT token
    public Date getExpirationDateFromToken(String token) {
        try {
            return getClaimFromToken(token, Claims::getExpiration);
        } catch (Exception e) {
            throw new InvalidJwtTokenException("Error getting expiration date from JWT token");
        }
    }

	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = getAllClaimsFromToken(token);
            return claimsResolver.apply(claims);
        } catch (Exception e) {
            throw new InvalidJwtTokenException("Invalid JWT token");
        }
    }
    
	// Retrieve any information from token using the secret key
    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException | UnsupportedJwtException | IllegalArgumentException e) {
            throw new InvalidJwtTokenException("Invalid JWT token");
        }
    }

	// Check if the token has expired
    private Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            throw new InvalidJwtTokenException("Expired JWT token");
        }
    }

	public int getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
            return claims.get("userId", Integer.class);
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException | UnsupportedJwtException | IllegalArgumentException e) {
            throw new InvalidJwtTokenException("Invalid JWT token");
        }
    }

	// Generate token for user
    public String generateToken(User user) {
        try {
            Map<String, Object> claims = new HashMap<>();
            return doGenerateToken(claims, user.getEmail());
        } catch (Exception e) {
            throw new JwtTokenCreationException("Error creating JWT token");
        }
    }
    
	// Generate token for user
    public String generateDonationToken(DonationAppUser user) {
        try {
            Map<String, Object> claims = new HashMap<>();
            return doGenerateToken(claims, user.getEmail());
        } catch (Exception e) {
            throw new JwtTokenCreationException("Error creating JWT token");
        }
    }

	//while creating the token -
	//1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
	//2. Sign the JWT using the HS512 algorithm and secret key.
	//3. According to JWS Compact Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
	//   compaction of the JWT to a URL-safe string 
	private String doGenerateToken(Map<String, Object> claims, String subject) {
        try {
            return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
        } catch (Exception e) {
            throw new JwtTokenCreationException("Error creating JWT token");
        }
    }

	// Validate User token
    public Boolean validateToken(String token, User user) {
        try {
            final String username = getUsernameFromToken(token);
            return (username.equals(user.getEmail()) && !isTokenExpired(token));
        } catch (Exception e) {
            throw new InvalidJwtTokenException("Invalid JWT token");
        }
    }

    // Validate token
    public Boolean validateDonationToken(String token, DonationAppUser user) {
        try {
            final String username = getUsernameFromToken(token);
            return (username.equals(user.getEmail()) && !isTokenExpired(token));
        } catch (Exception e) {
            throw new InvalidJwtTokenException("Invalid JWT token");
        }
    }
}