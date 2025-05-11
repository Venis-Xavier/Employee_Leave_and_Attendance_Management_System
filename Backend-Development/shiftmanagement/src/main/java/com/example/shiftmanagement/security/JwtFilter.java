package com.example.shiftmanagement.security;

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
        //System.out.println("authHeader: "+authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        //System.out.println("token: "+token);
        try {
            if (!jwtUtil.validateToken(token)) {
                log.warn("Invalid JWT token");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                System.out.println("valid token");

                return;
            }

            String employeeId = jwtUtil.extractEmployeeId(token);
            String roles = jwtUtil.extractRoles(token);
            String email = jwtUtil.extractEmailId(token);

            if (employeeId == null || roles == null) {
                log.error("Failed to extract claims from token");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            request.setAttribute("employeeId", employeeId);
            request.setAttribute("role", roles);
            request.setAttribute("emailId", email);
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
    }
}