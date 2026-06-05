package vector.StockManagement.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vector.StockManagement.model.User;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        MDC.put("http.method", method);
        MDC.put("http.path", requestURI);

        // Skip filter for OPTIONS requests and explicit public endpoints only
        if (method.equals("OPTIONS") ||requestURI.equals("/api/auth/register/super") || requestURI.equals("/api/auth/login") || requestURI.equals("/api/auth/refresh-token") || requestURI.equals("/api/auth/forgot-password") || requestURI.equals("/api/auth/request-password-reset") || requestURI.equals("/api/auth/reset-password") || requestURI.equals("/error") || requestURI.equals("/api/tenants/admin")) {
            logger.debug("Skipping JWT filter for request: {} {}", method, requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        logger.debug("Authorization Header: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ") || authHeader.equals("Bearer undefined")) {
            logger.debug("No valid Bearer token found, proceeding without authentication.");
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);
        logger.debug("JWT Token: {}", jwt);

        try {
            String userEmail = jwtService.extractUsername(jwt);
            MDC.put("user.email", userEmail);
            logger.debug("Extracted Email: {}", userEmail);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                logger.debug("UserDetails: {}", userDetails.getUsername());

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    List<String> authorities = jwtService.extractAuthorities(jwt);

                    // Extract and set tenant ID from JWT token
                    try {
                        Long tenantId = jwtService.extractTenantId(jwt);
                        if (tenantId != null) {
                            TenantContext.setTenantId(tenantId);
                            MDC.put("tenant.id", String.valueOf(tenantId));
                            logger.info("Set tenant context from JWT to ID: {}", tenantId);
                        } else {
                            // Handle SUPER_ADMIN case - set to 0L for global access
                            if (userDetails instanceof User user && user.getRole().name().equals("SUPER_ADMIN")) {
                                TenantContext.setTenantId(0L);
                                MDC.put("tenant.id", "0");
                                logger.info("Set tenant context to 0L for SUPER_ADMIN");
                            } else {
                                logger.warn("No tenant ID found in JWT token for user: {}", userEmail);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Failed to extract tenant ID from JWT: {}", e.getMessage());
                        // For SUPER_ADMIN, set to 0L as fallback
                        if (userDetails instanceof User user && user.getRole().name().equals("SUPER_ADMIN")) {
                            TenantContext.setTenantId(0L);
                            MDC.put("tenant.id", "0");
                            logger.info("Set tenant context to 0L for SUPER_ADMIN (fallback)");
                        }
                    }

                    List<SimpleGrantedAuthority> grantedAuthorities = authorities != null
                            ? authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
                            : List.of();
                    logger.debug("Authorities: {}", grantedAuthorities);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, grantedAuthorities.isEmpty() ? userDetails.getAuthorities() : grantedAuthorities
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("Authentication set in SecurityContext for user: {}", userEmail);
                } else {
                    logger.debug("JWT token is invalid for user: {}", userEmail);
                }
            } else {
                logger.debug("No email extracted or authentication already exists.");
            }
        } catch (Exception e) {
            logger.error("JWT authentication failed: {}", e.getMessage(), e);
            // Proceed without authentication instead of throwing an exception
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            logger.debug("JWT filter completed, cleared tenant context");
            MDC.clear();
        }
    }
}