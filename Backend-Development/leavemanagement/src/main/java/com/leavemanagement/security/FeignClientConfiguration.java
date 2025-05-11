package com.leavemanagement.security;


import feign.RequestInterceptor;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;

import org.springframework.security.core.context.SecurityContextHolder;

@Configuration

public class FeignClientConfiguration {

    @Bean

    public RequestInterceptor requestInterceptor() {

        return requestTemplate -> {

            // Retrieve the JWT token from SecurityContext

            String token = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();

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
 