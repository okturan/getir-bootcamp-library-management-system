package com.okturan.getirbootcamplibrarymanagementsystem.config;

import com.okturan.getirbootcamplibrarymanagementsystem.security.CustomAccessDeniedHandler;
import com.okturan.getirbootcamplibrarymanagementsystem.security.CustomAuthenticationEntryPoint;
import com.okturan.getirbootcamplibrarymanagementsystem.security.CustomUserDetailsService;
import com.okturan.getirbootcamplibrarymanagementsystem.security.JwtFilter;
import com.okturan.getirbootcamplibrarymanagementsystem.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider, 
                         CustomUserDetailsService userDetailsService,
                         CustomAuthenticationEntryPoint authenticationEntryPoint,
                         CustomAccessDeniedHandler accessDeniedHandler) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter(jwtTokenProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for dev
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll() // Allow H2 Console
                        .requestMatchers("/swagger-ui.html").permitAll() // Allow Swagger UI
                        .requestMatchers("/swagger-ui/**").permitAll() // Allow Swagger UI resources
                        .requestMatchers("/swagger-ui/index.html").permitAll() // Allow Swagger UI index
                        .requestMatchers("/webjars/**").permitAll() // Allow Swagger UI webjars
                        .requestMatchers("/v3/api-docs/**").permitAll() // Allow OpenAPI docs
                        .requestMatchers("/v3/api-docs.yaml").permitAll() // Allow OpenAPI YAML
                        .requestMatchers("/api/auth/**").permitAll() // Allow authentication endpoints
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin()) // Allow H2 Console frames
                )
                .formLogin(form -> form.disable()) // Disable login form (optional, for pure APIs)
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
