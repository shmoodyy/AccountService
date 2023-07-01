package account.business.configs.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Autowired
    private final UserDetailsService userDetailsService;

    @Autowired
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests()
                .requestMatchers(HttpMethod.POST, "/api/auth/changepass")
                    .authenticated()


                .requestMatchers(HttpMethod.GET, "/api/empl/payment")
                    .hasAnyAuthority("ROLE_USER", "ROLE_ACCOUNTANT")

                .requestMatchers(HttpMethod.POST, "/api/acct/payments")
                    .hasAuthority("ROLE_ACCOUNTANT")
                .requestMatchers(HttpMethod.PUT, "/api/acct/payments")
                    .hasAuthority("ROLE_ACCOUNTANT")

                .requestMatchers(HttpMethod.GET, "/api/security/events", "/api/security/events/")
                    .hasAuthority("ROLE_AUDITOR")

                .requestMatchers(HttpMethod.PUT, "/api/admin/user/role")
                    .hasAuthority("ROLE_ADMINISTRATOR")
                .requestMatchers(HttpMethod.GET, "/api/admin/user", "/api/admin/user/")
                    .hasAuthority("ROLE_ADMINISTRATOR")
                .requestMatchers(HttpMethod.DELETE, "/api/admin/user", "/api/admin/user/"
                        , "/api/admin/user/*", "/api/admin/user/*/")
                    .hasAuthority("ROLE_ADMINISTRATOR")
                .requestMatchers(HttpMethod.PUT, "/api/admin/user/access")
                    .hasAuthority("ROLE_ADMINISTRATOR")

                .anyRequest().permitAll()


                .and()
                .httpBasic()

                .authenticationEntryPoint(restAuthenticationEntryPoint) // Handles auth error
                .and()
                .exceptionHandling().accessDeniedHandler(customAccessDeniedHandler())
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // no session
                .and().csrf().disable().headers().frameOptions().disable(); // for Postman, the H2 console

        http.authenticationProvider(authenticationProvider()); // Set authentication provider

        return http.build();
    }

    @Bean
    public AccessDeniedHandler customAccessDeniedHandler(){
        return new CustomAccessDeniedHandler();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(getEncoder());
        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder(13);
    }
}