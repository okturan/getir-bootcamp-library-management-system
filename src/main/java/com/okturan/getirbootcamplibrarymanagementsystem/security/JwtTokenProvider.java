package com.okturan.getirbootcamplibrarymanagementsystem.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private static final String AUTHORITIES_KEY = "auth";
    private static final long TOKEN_VALIDITY_MILLISECONDS = 86400000; // 24 hours

    private final SecretKey key;
    private final UserDetailsService userDetailsService;

    public JwtTokenProvider(UserDetailsService userDetailsService) {
        // Generate a secure key for HS512 algorithm
        // In production, this should be externalized and properly secured
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        this.userDetailsService = userDetailsService;
    }

    public String createToken(Authentication authentication) {
        logger.debug("Creating JWT token for user: {}", authentication.getName());

        try {
            String authorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));

            logger.debug("User authorities: {}", authorities);

            long now = (new Date()).getTime();
            Date validity = new Date(now + TOKEN_VALIDITY_MILLISECONDS);

            String token = Jwts.builder()
                    .setSubject(authentication.getName())
                    .claim(AUTHORITIES_KEY, authorities)
                    .signWith(key, SignatureAlgorithm.HS512)
                    .setExpiration(validity)
                    .compact();

            logger.info("JWT token created successfully for user: {}", authentication.getName());
            logger.debug("Token expiration: {}", validity);

            return token;
        } catch (Exception e) {
            logger.error("Error creating JWT token for user: {}", authentication.getName(), e);
            throw e;
        }
    }

    public Authentication getAuthentication(String token) {
        logger.debug("Getting authentication from JWT token");

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            logger.debug("Username from token: {}", username);

            // Load user details from the database
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            logger.debug("User details loaded for: {}", username);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, token, userDetails.getAuthorities());

            logger.info("Authentication created for user: {}", username);
            return authentication;
        } catch (Exception e) {
            logger.error("Error getting authentication from token", e);
            throw e;
        }
    }

    public boolean validateToken(String token) {
        logger.debug("Validating JWT token");

        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            logger.debug("JWT token is valid");
            return true;
        } catch (Exception e) {
            logger.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
}
