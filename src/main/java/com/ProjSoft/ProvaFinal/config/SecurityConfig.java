package com.ProjSoft.ProvaFinal.config;

import java.time.Clock;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;

@Configuration
public class SecurityConfig {
  private static String[] roleNames(String value) {
    return Arrays.stream(value.split(","))
        .map(String::trim)
        .filter(role -> !role.isEmpty())
        .toArray(String[]::new);
  }

  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  JwtDecoder jwtDecoder(
      @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer,
      @Value("${app.auth0.audience}") String audience) {
    NimbusJwtDecoder decoder = JwtDecoders.fromIssuerLocation(issuer);
    OAuth2TokenValidator<Jwt> aud =
        jwt ->
            jwt.getAudience() != null && jwt.getAudience().contains(audience)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Audience inválida", null));
    decoder.setJwtValidator(
        new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(issuer), aud));
    return decoder;
  }

  @Bean
  JwtAuthenticationConverter jwtAuthenticationConverter(
      @Value("${app.auth0.roles-claim}") String rolesClaim) {
    var converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(
        jwt -> {
          var scopeConverter = new JwtGrantedAuthoritiesConverter();
          Collection<GrantedAuthority> authorities = new ArrayList<>(scopeConverter.convert(jwt));
          Object claim = jwt.getClaims().get(rolesClaim);
          if (claim instanceof Collection<?> roles) {
            roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .forEach(authorities::add);
          }
          return authorities;
        });
    return converter;
  }

  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      JwtAuthenticationConverter converter,
      @Value("${app.api.path}") String path,
      @Value("${app.security.read-roles}") String readRoles,
      @Value("${app.security.write-roles}") String writeRoles)
      throws Exception {
    return http.csrf(csrf -> csrf.disable())
        .cors(cors -> {})
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, path, path + "/**")
                    .hasAnyRole(roleNames(readRoles))
                    .requestMatchers(HttpMethod.POST, path, path + "/**")
                    .hasAnyRole(roleNames(writeRoles))
                    .requestMatchers(HttpMethod.PUT, path, path + "/**")
                    .hasAnyRole(roleNames(writeRoles))
                    .requestMatchers(HttpMethod.PATCH, path, path + "/**")
                    .hasAnyRole(roleNames(writeRoles))
                    .requestMatchers(HttpMethod.DELETE, path, path + "/**")
                    .hasAnyRole(roleNames(writeRoles))
                    .anyRequest()
                    .denyAll())
        .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)))
        .build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource(
      @Value("${app.frontend-origins}") String origins) {
    var cors = new CorsConfiguration();
    cors.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).toList());
    cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    cors.setExposedHeaders(List.of("Location"));
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cors);
    return source;
  }
}
