package com.employeemanagement.authFilter;

import java.io.IOException;

import java.util.UUID;
 
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Lazy;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import com.employeemanagement.service.EmployeeService;
import com.employeemanagement.service.JWTService;

import jakarta.servlet.FilterChain;

import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
 
@Slf4j
@Component
public class AuthFilter extends OncePerRequestFilter {

    @Autowired
    private JWTService jwtService;

    @Lazy
    @Autowired
    EmployeeService userService;
 
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)

            throws ServletException, IOException {
 
        String authHeader = request.getHeader("AUTHORIZATION");

        String token = null;

        String email = null;

        String employeeId = null;

        String role = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            log.info("token: "+token);
            email = jwtService.extractEmail(token);
            log.info("email : "+email);

            employeeId = jwtService.extractEmployeeId(token);
            log.info("employeeId:"+employeeId);

            role = jwtService.extractRole(token);
            log.info("role: "+role);

        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userService.loadUserByUsername(email);

            if (jwtService.validateToken(token, userDetails)) {

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);

            }

        }

        request.setAttribute("employeeId", employeeId);
        request.setAttribute("email", email);
        request.setAttribute("role", role);

        filterChain.doFilter(request, response);

    }

}
 