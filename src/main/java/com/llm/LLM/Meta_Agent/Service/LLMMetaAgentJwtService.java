package com.llm.LLM.Meta_Agent.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class LLMMetaAgentJwtService {
	
	private final String SECRET_STRING="itmustbegreatethan32lengthksajkjadkjadsjaskdjkjdkdjaskjda";
	private final SecretKey SECRET_KEY=Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));
	private final long expiration_time_ms=1200000;

	//It will genrate tokens
	public String genrateToken(String username) {
		return Jwts.builder()
				.subject(username)
				.issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis()+expiration_time_ms))
				.signWith(SECRET_KEY)
				.compact();	
	}
	
	public boolean isTokenValid(String token,UserDetails userdetails) {
		final String username=extractUsername(token);
		return (username.equals(userdetails.getUsername()) && !isTokenExpired(token));
	}
	
	public boolean isTokenExpired(String token) {
		return extractClaim(token,Claims::getExpiration).before(new Date());
	}
	
	public String extractUsername(String token){
		return extractClaim(token,Claims::getSubject);
	}
	
	//It will extract information and verify with signature
	private<T> T extractClaim(String token,Function<Claims,T> claimResolver) {
		final Claims claims=Jwts.parser()
				.verifyWith(SECRET_KEY)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		return claimResolver.apply(claims);
	}
}
