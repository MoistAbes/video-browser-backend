package dev.zymion.video.browser.app.services.security;

import dev.zymion.video.browser.app.config.properties.JwtProperties;
import dev.zymion.video.browser.app.enums.RoleEnum;
import dev.zymion.video.browser.app.models.entities.user.UserInfoEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Service class responsible for generating and validating JWT tokens.
 * Used for stateless authentication in the Zymflix application.
 */
@Service
public class JwtService {


    private final String SECRET_KEY;


    /**
     * Secret key used to sign the JWT tokens.
     * Must be at least 256 bits for HS256 algorithm.
     * In production, store this securely (e.g., environment variable).
     */
    public JwtService(JwtProperties jwtProperties) {
        this.SECRET_KEY = jwtProperties.getSecretKey();
    }



    /**
     * Token expiration time in milliseconds.
     * Currently set to 24 hour.
     */
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24h
//    private static final long EXPIRATION_TIME = 1000 * 60; // 1 min

    /**
     * Returns the signing key used for JWT token creation and validation.
     *
     * @return HMAC SHA key derived from the secret string.
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    /**
     * Generates a JWT token for the given user, embedding their username, ID, and roles.
     *
     * @param user the authenticated user entity
     * @return a signed JWT token string
     */
    public String generateToken(UserInfoEntity user) {
        return Jwts.builder()
                .setSubject(user.getUsername()) // Used as the principal identifier
                .claim("id", user.getId()) // Unique user ID
                .claim("roles", user.getRoles().stream()
                        .map(roleEntity -> roleEntity.getName().name())
                        .collect(Collectors.toList()))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates the token by checking its expiration and matching the username.
     *
     * @param token    the JWT token to validate.
     * @param username the expected username.
     * @return true if the token is valid and matches the username.
     */
    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username)) && !isTokenExpired(token);
    }

    /**
     * Extracts the username (subject) from the JWT token.
     *
     * @param token the JWT token.
     * @return the username stored in the token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the user ID from the JWT token.
     *
     * @param token the JWT token
     * @return the user ID as a Long
     */
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("id", Long.class);
    }


    public List<RoleEnum> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        List<String> roleNames = claims.get("roles", List.class);
        return roleNames.stream()
                .map(RoleEnum::valueOf)
                .collect(Collectors.toList());
    }

    /**
     * Extracts the expiration date from the JWT token.
     *
     * @param token the JWT token.
     * @return the expiration date.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from the JWT token using a resolver function.
     *
     * @param token          the JWT token.
     * @param claimsResolver a function to extract a specific claim.
     * @param <T>            the type of the claim.
     * @return the extracted claim.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses the JWT token and extracts all claims.
     *
     * @param token the JWT token.
     * @return the claims contained in the token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Checks if the token is expired.
     *
     * @param token the JWT token.
     * @return true if the token is expired.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}

