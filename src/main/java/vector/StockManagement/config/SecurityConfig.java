package vector.StockManagement.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final TenantFilter tenantFilter;
    private final LogoutHandler logoutHandler;

    private static final String[] SWAGGER_WHITELIST = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-ui/index.html",
            "/swagger-ui/**",
            "/webjars/**"
    };

    private static final String[] WHITE_LIST_URL = {
            "/api/auth/login",
            "/api/auth/refresh-token",
            "/api/auth/forgot-password",
            "/api/auth/request-password-reset",
            "api/auth/register/super",
            "/api/auth/reset-password",
            "/api/tenants/admin",
            "/v2/api-docs",
            "/v3/api-docs",
            "/api/contact/pricing",
            "/api/contact/request",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui/**",
            "/webjars/**",
            "/swagger-ui.html",
            "/swagger-ui/index.html",
            "/error"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        req -> req

                                .requestMatchers(WHITE_LIST_URL).permitAll()
                                .requestMatchers(SWAGGER_WHITELIST).permitAll()
                                .requestMatchers("/api/auth/register").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGING_DIRECTOR")
                                .requestMatchers("/api/tenants/admin").hasRole("SUPER_ADMIN")
                                .requestMatchers("/uploads/**", "/favicon.ico").permitAll()
                                .requestMatchers("/api/tenants/{id}").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
                                .requestMatchers(HttpMethod.GET, "/api/users").permitAll()//I wanted to this api to be secure, put there's no problem , we'll fix it later
                                .requestMatchers(HttpMethod.POST, "/api/tenants/***").hasRole("SUPER_ADMIN")
                                .requestMatchers("/api/management/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                                .requestMatchers(HttpMethod.GET, "/api/management/**").hasAnyRole("ADMIN_READ", "MANAGER_READ")
                                .requestMatchers(HttpMethod.POST, "/api/management/**").hasAnyRole("ADMIN_CREATE", "MANAGER_CREATE")
                                .requestMatchers(HttpMethod.PUT, "/api/management/**").hasAnyRole("ADMIN_UPDATE", "MANAGER_UPDATE")
                                .requestMatchers(HttpMethod.DELETE, "/api/management/**").hasAnyRole("ADMIN_DELETE", "MANAGER_DELETE")
                                .requestMatchers(HttpMethod.POST, "/api/register/super").permitAll()
                                .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(tenantFilter, JwtAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://10.11.72.159:5173/","https://e-shop-test-v1.vercel.app/","https://sales.rw" ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}