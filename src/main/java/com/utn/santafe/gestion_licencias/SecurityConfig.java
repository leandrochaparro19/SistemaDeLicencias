package com.utn.santafe.gestion_licencias;

import com.utn.santafe.gestion_licencias.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return (request, response, authentication) -> {
            String role = authentication.getAuthorities().iterator().next().getAuthority();

            if ("ROLE_SUPERUSER".equals(role)) {
                response.sendRedirect("/dashboard");
            } else if ("ROLE_ADMIN_CON_PERMISOS".equals(role)) {
                response.sendRedirect("/dashboard");
            } else if ("ROLE_ADMIN".equals(role)) {
                response.sendRedirect("/");
            } else {
                response.sendRedirect("/dashboard");
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                /*.authorizeHttpRequests(authz -> authz
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/h2-console/**").permitAll()
                        .requestMatchers("/usuarios/**").hasRole("SUPERUSER")
                        .anyRequest().authenticated()
                )*/
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("dni")
                        .passwordParameter("password")
                        .successHandler(customSuccessHandler())
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .userDetailsService(userDetailsService)
                //.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                //.headers(headers -> headers.frameOptions().sameOrigin())
                .authorizeHttpRequests((requests) -> requests.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions().disable());
                ;

        return http.build();
    }
}