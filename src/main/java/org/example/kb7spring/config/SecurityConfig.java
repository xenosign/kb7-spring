package org.example.kb7spring.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.security.filter.JwtAuthenticationFilter;
import org.example.kb7spring.security.filter.JwtLoginFilter;
import org.example.kb7spring.security.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final JwtTokenProvider jwtTokenProvider;

    public CharacterEncodingFilter encodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        return filter;
    }

//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http.authorizeRequests()
//                .antMatchers("/").permitAll()
//                .antMatchers("/user/**").permitAll()
//                .antMatchers("/admin/**").access("hasRole('ROLE_ADMIN')")
//                .antMatchers("/student/**").access("hasAnyRole('ROLE_ADMIN', 'ROLE_MEMBER')")
//                .antMatchers("/**").authenticated();
//
//        http.formLogin()
//                .loginPage("/user/login")
//                .loginProcessingUrl("/user/login")
//                .defaultSuccessUrl("/user/login-success")
//                .failureUrl("/user/login-failure");
//
//        http.logout()
//                .logoutUrl("/user/logout")
//                .invalidateHttpSession(true)
//                .deleteCookies("JSESSIONID", "remember-me")
//                .logoutSuccessUrl("/user/login")
//                .permitAll();
//
//        http.addFilterBefore(encodingFilter(), CsrfFilter.class);
//    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .httpBasic().disable()
                .csrf().disable()
                .formLogin().disable();

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/user/**").permitAll()
                .antMatchers("/oauth/**").permitAll()
                .antMatchers("/api/index/**").permitAll()
                .antMatchers("/auth/admin").hasRole("ADMIN")
                .antMatchers("/auth/member").hasAnyRole("ADMIN", "MEMBER")
                .antMatchers("/auth/**").authenticated()
                .antMatchers("/**").authenticated();

        http.addFilterBefore(
                new JwtLoginFilter(authenticationManager(), jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class
        );

        http.addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class
        );
    }
}
