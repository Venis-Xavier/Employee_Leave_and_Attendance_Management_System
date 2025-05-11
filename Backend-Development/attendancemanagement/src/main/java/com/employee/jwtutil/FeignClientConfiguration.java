package com.employee.jwtutil;
import feign.RequestInterceptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
 
import org.springframework.context.annotation.Configuration;
 
import org.springframework.security.core.context.SecurityContextHolder;
 
@Configuration
 
public class FeignClientConfiguration {
 
    @Bean
    public RequestInterceptor requestInterceptor(@Value("${token}") String token) {
 
        return requestTemplate -> {
 
            // Retrieve the JWT token from SecurityContext
 
            //String token = "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJlbXBsb3llZTAwMSIsImlhdCI6MTc0NDc4NDUzNSwiZXhwIjoxNzUyNTYwNTM1fQ.xkme_bxHPiKaGuZSjaSfHdP7W8f1JQKm-Pwx38MjptP9n1d_mGCMq5zH9KAIjsEW";
             //(String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
           // token="scheduler.token"
            
            System.out.println("Feign Request Interceptor Invoked");
 
            System.out.println("Token Passed: " + token);
 
            if (token != null && !token.isEmpty()) {
 
                requestTemplate.header("Authorization", "Bearer " + token);
 
                System.out.println("Authorization Header Set: Bearer " + token);
 
            } else {
 
                System.out.println("Authorization Header Missing");
 
            }
 
        };
 
    }
 
}
