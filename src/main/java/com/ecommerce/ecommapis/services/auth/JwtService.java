package com.ecommerce.ecommapis.services.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService
{
    @Value("${jwt.secret}")
    private String secretKey;

    public String generateToken(final String username, final String role)
    {
        final Map<String, Object> claims = new HashMap<>();

        claims.put("role", role); // add role to the JWT claims

        return Jwts
                .builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 30 * 60 * 60 * 1000))
                .and()
                .signWith(getKey())
                .compact();
    }

    private SecretKey getKey()
    {
        final byte[] KeyBytes = Decoders.BASE64.decode(secretKey);

        return Keys.hmacShaKeyFor(KeyBytes);
    }

    // extract the username from jwt token
    public String extractUserName(final String jwtToken)
    {
        return extractClaim(jwtToken, Claims::getSubject);
    }

    private <T> T extractClaim(final String jwtToken, final Function<Claims, T> claimResolver)
    {
        final Claims claims = extractAllClaims(jwtToken);

        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(final String jwtToken)
    {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(jwtToken)
                .getPayload();
    }

    public boolean validateToken(final String jwtToken, final UserDetails userDetails)
    {
        final String userName = extractUserName(jwtToken);

        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(jwtToken));
    }

    private boolean isTokenExpired(final String jwtToken)
    {
        return extractExpiration(jwtToken).before(new Date());
    }

    private Date extractExpiration(final String jwtToken)
    {
        return extractClaim(jwtToken, Claims::getExpiration);
    }
}
