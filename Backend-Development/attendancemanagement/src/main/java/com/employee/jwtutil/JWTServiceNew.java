package com.employee.jwtutil;

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

import com.employee.entity.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
@Service
public class JWTServiceNew {
	
	
	
	
	private String secretKey = "qwertyuioppoiuytrewqasdfghjkllkjhgfdsazxcvbnmmnbvcxz";
	public String getToken(String employeeId, Role role, String email) {
		Map<String, String> claims = new HashMap<>();
		claims.put("employeeId", employeeId);
		claims.put("role", role.toString());
		claims.put("emailId",email);
		return Jwts.builder().subject(email).claims().add(claims).issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + 400 * 24 * 60 * 60 * 60))
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
	public boolean validateToken(String token) {
		//final String userName = extractEmail(token);
		return !isTokenExpired(token);
	}
	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}
	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}	
    
    public static void main(String [] rags) {
    	JWTServiceNew service=new JWTServiceNew();
    	String token=service.getToken("5422586",Role.valueOf("MANAGER"),"surya.prabha@cognizant.com");
    	System.out.println(token);
    	
    }
}


