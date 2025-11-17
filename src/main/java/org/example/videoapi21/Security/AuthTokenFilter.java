package org.example.videoapi21.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    private JwtUtils jwtUtils;
    private UserDetailsServiceImpl userDetailsService;

    public AuthTokenFilter(UserDetailsServiceImpl userDetailsService, JwtUtils jwtUtils) {
        this.userDetailsService = userDetailsService;
        this.jwtUtils = jwtUtils;
    }

    private static Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException, UsernameNotFoundException {

        // 🔥 LOGI DIAGNOSTYCZNE
        logger.info("=== AuthTokenFilter START ===");
        logger.info("Request: {} {}", request.getMethod(), request.getRequestURI());
        logger.info("Content-Type: {}", request.getContentType());
        logger.info("Authorization Header: {}", request.getHeader("Authorization"));

        try {
            String jwt = JwtUtils.getJwtFromRequest(request);
            logger.info("Extracted JWT: {}", jwt);

            if (jwt != null && jwtUtils.validateToken(jwt)) {
                logger.info("JWT is valid");

                String username = jwtUtils.extractUsername(jwt);
                logger.info("Extracted username: {}", username);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.info("Authentication set for user {}", username);
            } else {
                logger.warn("JWT is NULL or INVALID");
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage(), e);
        }

        logger.info("=== AuthTokenFilter END ===\n");

        filterChain.doFilter(request, response);
    }
}

