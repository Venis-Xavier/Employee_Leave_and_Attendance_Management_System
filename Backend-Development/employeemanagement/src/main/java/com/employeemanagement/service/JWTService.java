package com.employeemanagement.service;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
 
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
 
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.employeemanagement.entity.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
@Service
public class JWTService {
	private String secretKey = "qwertyuioppoiuytrewqasdfghjkllkjhgfdsazxcvbnmmnbvcxz";
	/*public JWTService() {
		try {
			KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
			SecretKey sk = keyGen.generateKey();
			secretKey = Base64.getEncoder().encodeToString(sk.getEncoded());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
	public String getToken(String employeeId, Role role, String email) {
		Map<String, String> claims = new HashMap<>();
		claims.put("employeeId", employeeId);
		claims.put("role", role.toString());
		claims.put("emailId",email);
		return Jwts.builder().subject(email).claims().add(claims).issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + 60 * 60 * 1000))
				.and().signWith(getKey()).compact();
	}
	private SecretKey getKey() {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		return Keys.hmacShaKeyFor(keyBytes);
	}
	public String extractEmail(String token) {
		return extractClaim(token, Claims::getSubject);
	}
	public String extractEmployeeId(String token) {
		return extractClaim(token, claims -> claims.get("employeeId", String.class));
	}
	public String extractRole(String token) {
		return extractClaim(token, claims -> claims.get("role", String.class));
	}
	private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
		final Claims claims = extractAllClaims(token);
		return claimResolver.apply(claims);
	}
	private Claims extractAllClaims(String token) {
		return Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();
	}
	public boolean validateToken(String token, UserDetails userDetails) {
		final String userName = extractEmail(token);
		return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}
	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}
	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}
}