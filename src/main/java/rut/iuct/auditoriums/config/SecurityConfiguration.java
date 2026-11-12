package rut.iuct.auditoriums.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import rut.iuct.auditoriums.repository.EmployeeAccountRepository;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Configuration
public class SecurityConfiguration {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean
    UserDetailsService users(EmployeeAccountRepository accounts) {
        return login -> accounts.findByLogin(login).map(account -> User.withUsername(account.getLogin())
                .password(account.getPasswordHash()).roles("EMPLOYEE").build())
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException(login));
    }
    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/login", "/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(errors -> errors.defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(UNAUTHORIZED), request -> request.getRequestURI().startsWith("/api/")))
                .formLogin(login -> login.loginPage("/login").defaultSuccessUrl("/auditoriums", true).permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/auditoriums"))
                .httpBasic(Customizer.withDefaults()).build();
    }
}
