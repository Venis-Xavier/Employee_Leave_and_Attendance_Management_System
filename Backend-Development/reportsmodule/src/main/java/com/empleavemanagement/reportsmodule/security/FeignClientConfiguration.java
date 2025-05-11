//package com.empleavemanagement.reportsmodule.security;
//
//import feign.RequestInterceptor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.core.context.SecurityContextHolder;
// 
//@Configuration
//public class FeignClientConfiguration {
// 
//    @Bean
//    public RequestInterceptor requestInterceptor() {
//        return requestTemplate -> {
//            // Retrieve the JWT token from SecurityContext
//            String token = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
//            System.out.println("Feign Request Interceptor Invoked");
//            System.out.println("Token Passed: " + token);
// 
//            if (token != null && !token.isEmpty()) {
//                requestTemplate.header("Authorization", "Bearer " + token);
//                System.out.println("Authorization Header Set: Bearer " + token);
//            } else {
//                System.out.println("Authorization Header Missing");
//            }
//        };
//        
//    }
//}

package com.empleavemanagement.reportsmodule.security;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class FeignClientConfiguration {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // Retrieve the Authentication object from SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated() && authentication.getCredentials() != null) {
                // Retrieve the JWT token from the Authentication object
                String token = authentication.getCredentials().toString();
                System.out.println("Feign Request Interceptor Invoked");
                System.out.println("Token Passed: " + token);

                if (token != null && !token.isEmpty()) {
                    // Set the Authorization header with the Bearer token
                    requestTemplate.header("Authorization", "Bearer " + token);
                    System.out.println("Authorization Header Set: Bearer " + token);
                } else {
                    System.out.println("Authorization Header Missing");
                }
            } else {
                System.out.println("Authentication or Credentials Missing in SecurityContext");
            }
        };
    }
}