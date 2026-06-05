package vector.StockManagement.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import vector.StockManagement.model.User;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class TenantFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(TenantFilter.class);

    // Whitelisted endpoints that don't require tenant context
    private static final List<String> WHITELIST = Arrays.asList(
            "/api/auth/login",
            "api/auth/register/super" +
            "/api/auth/refresh-token",
            "/api/auth/forgot-password",
            "/api/auth/request-password-reset",
            "/api/auth/reset-password",
            "/api/tenants/admin",
            "/error",
            "/v3/api-docs",
            "/swagger-ui",
            "/actuator"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        MDC.put("http.path", requestURI);
        MDC.put("http.method", method);

        logger.debug("TenantFilter processing request: {} {}", method, requestURI);

        // Skip filter for whitelisted endpoints
        if (isWhitelisted(requestURI)) {
            logger.debug("Skipping tenant filter for whitelisted URI: {}", requestURI);
            chain.doFilter(request, response);
            return;
        }

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && !(auth instanceof AnonymousAuthenticationToken)) {
                if (auth.getPrincipal() instanceof User user) {
                    logger.debug("Processing authenticated user: {} with role: {}", user.getEmail(), user.getRole());
                    
                    if (user.getTenant() != null) {
                        TenantContext.setTenantId(user.getTenant().getId());
                        MDC.put("tenant.id", String.valueOf(user.getTenant().getId()));
                        logger.info("Set tenant context from User principal to ID: {} for user: {} on URI: {}", 
                                   user.getTenant().getId(), user.getEmail(), requestURI);
                    } else if (user.getRole().name().equals("SUPER_ADMIN")) {
                        // SUPER_ADMIN gets global access with tenant ID 0
                        TenantContext.setTenantId(0L);
                        MDC.put("tenant.id", "0");
                        logger.info("Set tenant context to 0L for SUPER_ADMIN: {} on URI: {}", user.getEmail(), requestURI);
                    } else {
                        logger.error("TENANT ERROR - User {} has no tenant but is not SUPER_ADMIN. Role: {} | URI: {} {}", 
                                   user.getEmail(), user.getRole(), method, requestURI);
                        // This could be a security issue - user without tenant trying to access protected resources
                    }
                } else {
                    logger.warn("Principal is not a User instance: {} for URI: {} {}", 
                               auth.getPrincipal().getClass(), method, requestURI);
                }
            } else {
                logger.warn("No valid authentication found for URI: {} {} - tenant context will remain unset", 
                           method, requestURI);
            }

            chain.doFilter(request, response);
        } catch (Exception e) {
            logger.error("TENANT FILTER ERROR - Exception in TenantFilter for URI: {} {} | Error: {}", 
                        method, requestURI, e.getMessage(), e);
            throw e;
        } finally {
            // Note: Don't clear context here as JwtAuthenticationFilter will handle it
            logger.debug("TenantFilter completed for URI: {} {}", method, requestURI);
            MDC.clear();
        }
    }

    private boolean isWhitelisted(String requestURI) {
        return WHITELIST.stream().anyMatch(requestURI::equals);
    }
}
