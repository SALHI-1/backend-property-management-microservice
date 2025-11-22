package com.lsiproject.app.propertymanagementmicroservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;


import java.io.IOException;
import java.util.Set;

/**
 * Filter to process JWT token included in the Authorization header.
 * It operates in a stateless manner by extracting user claims directly from the token,
 * eliminating the need for a local UserDetails service or database query.
 */
@AllArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        // 1. Check for token presence
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            // Check if token is valid and not expired
            if (jwtUtil.isTokenValid(jwt)) {

                // 2. Extract ALL necessary claims directly from the token
                Long userId = jwtUtil.extractUserId(jwt);
                String walletAddress = jwtUtil.extractWalletAddress(jwt);
                Set<String> roles = jwtUtil.extractRoles(jwt);

                // 3. Check security context (Only authenticate if not already authenticated)
                if (walletAddress != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // 4. Create the Stateless UserPrincipal from JWT claims
                    UserPrincipal userPrincipal = new UserPrincipal(userId, walletAddress, roles);

                    // 5. Update security context
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userPrincipal,
                            null,
                            userPrincipal.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Log the failure (e.g., token expired, invalid signature, or claims missing)
            System.err.println("Stateless JWT processing failed: " + e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}