package com.okturan.getirbootcamplibrarymanagementsystem.config;

import com.okturan.getirbootcamplibrarymanagementsystem.security.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	// Define public endpoints in a single array to reduce maintenance overhead
	private static final String[] PUBLIC = { "/h2-console/**", "/swagger-ui.html", "/swagger-ui/**",
			"/swagger-ui/index.html", "/webjars/**", "/v3/api-docs/**", "/v3/api-docs.yaml", "/api/auth/register",
			"/api/auth/login" };

	private final JwtTokenProvider jwtTokenProvider;

	private final CustomUserDetailsService userDetailsService;

	private final CustomAuthenticationEntryPoint authenticationEntryPoint;

	private final CustomAccessDeniedHandler accessDeniedHandler;

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
		http.csrf(csrf -> csrf.disable()) // Disable CSRF for dev
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						// Public endpoints - grouped for easier maintenance
						.requestMatchers(PUBLIC)
						.permitAll() // Allow all public endpoints

						// Admin-only endpoints
						.requestMatchers("/api/auth/admin/**")
						.hasAuthority("ROLE_ADMIN") // Admin-only registration

						// Admin and Librarian endpoints
						// Make POST /api/books specific
						.requestMatchers(HttpMethod.POST, "/api/books")
						.hasAnyAuthority("ROLE_ADMIN", "ROLE_LIBRARIAN")
						// Keep /** for PUT and DELETE as they typically involve path variables like /api/books/{id}
						.requestMatchers(HttpMethod.PUT, "/api/books/**")
						.hasAnyAuthority("ROLE_ADMIN", "ROLE_LIBRARIAN")
						.requestMatchers(HttpMethod.DELETE, "/api/books/**")
						.hasAnyAuthority("ROLE_ADMIN", "ROLE_LIBRARIAN")

						// All authenticated users (including patrons) can access read-only
						// endpoints. /** covers /api/books, /api/books/{id}, /api/books/search, etc.
						.requestMatchers(HttpMethod.GET, "/api/books/**")
						.authenticated()

						// Any other request requires authentication
						.anyRequest()
						.authenticated())
				.exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint)
						.accessDeniedHandler(accessDeniedHandler))
				.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()) // Allow
				         // H2
				         // Console
				         // frames
				)
				.formLogin(form -> form.disable()) // Disable login form (optional, for pure
				// APIs)
				.authenticationProvider(authenticationProvider())
				.addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

}