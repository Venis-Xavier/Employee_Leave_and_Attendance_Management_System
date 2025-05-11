package com.leavemanagement.security;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private JWTService jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
    	
    	
    	String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            if (!jwtUtil.validateToken(token)) {
                log.warn("Invalid JWT token");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String employeeId = jwtUtil.extractEmployeeId(token);
            String roles = jwtUtil.extractRoles(token);

            if (employeeId == null || roles == null) {
                log.error("Failed to extract claims from token");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            request.setAttribute("employeeId", employeeId);
            log.info("Added employeeId to request attributes: {}", request.getAttribute("employeeId"));

            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(roles));
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(employeeId, token, authorities);
            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (Exception e) {
            log.error("Error processing token: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        chain.doFilter(request, response);
//        String authHeader = request.getHeader("Authorization");
//
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            chain.doFilter(request, response);
//            return;
//        }
//
//        String token = authHeader.substring(7);
//
//        if (!jwtUtil.validateToken(token)) {
//            chain.doFilter(request, response);
//            return;
//        }
//
//        // Extract user details from JWT
//        String employeeId = jwtUtil.extractEmployeeId(token);
//        String roles = jwtUtil.extractRoles(token);
//
//        System.out.println("Extracted Role: " + roles);
//        System.out.println("Extracted employee ID: " + employeeId);
//        
//        request.setAttribute("employee ID:", employeeId);
//        System.out.printf("Added employeeId to request attributes: {}", request.getAttribute("employeeId"));
//        
//        if (roles == null || roles.trim().isEmpty()) {
//            throw new IllegalArgumentException("A granted authority textual representation is required");
//        }
//
//        // Convert roles to GrantedAuthority list
//        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(roles));
//
//        // Create UserDetails object
//        UserDetails userDetails = new User(employeeId, "N/A", authorities);
//
//        // Set authentication in security context
//        UsernamePasswordAuthenticationToken authToken =
//                new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
//        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//        SecurityContextHolder.getContext().setAuthentication(authToken);
//        
//        
//        chain.doFilter(request, response);
    }
}