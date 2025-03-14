package prati.projeto.redeSocial.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import prati.projeto.redeSocial.modal.entity.CustomOAuth2User;
import prati.projeto.redeSocial.security.JwtAuthenticationFilter;
import prati.projeto.redeSocial.security.JwtService;
import prati.projeto.redeSocial.service.auth.CustomOAuth2UserService;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtService jwtService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
                    .requestMatchers("/atividades").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/libris/auth/**", "/login/**", "/oauth2/**", "/libris/usuario/reset-password/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                .requestMatchers("/libris/usuario/**", "/libris/perfil/**", "/libris/comentarios/**",
                    "/libris/resenhas/**", "/libris/status/**", "/libris/relacionamentos/**," +
                        "libris/curtidas/**", "/libris/posts/**", "/libris/atividade").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/libris/livro/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/libris/livro/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/libris/livro/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/libris/livro/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler((request, response, authentication) -> {
                    CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();
                    String accessToken = jwtService.gerarTokenO2auth(oauth2User);
                    String refreshToken = jwtService.gerarRefreshTokenO2auth(oauth2User);
                    response.sendRedirect("http://localhost:5173/login?token=" + accessToken + "&refreshToken=" + refreshToken);
                })
            )

            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
            .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
