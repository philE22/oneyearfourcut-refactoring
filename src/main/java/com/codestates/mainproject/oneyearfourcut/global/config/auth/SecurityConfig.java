package com.codestates.mainproject.oneyearfourcut.global.config.auth;

import com.codestates.mainproject.oneyearfourcut.domain.member.service.MemberService;
import com.codestates.mainproject.oneyearfourcut.global.config.auth.filter.JwtVerificationFilter;
import com.codestates.mainproject.oneyearfourcut.global.config.auth.handler.MemberAuthenticationEntryPoint;
import com.codestates.mainproject.oneyearfourcut.global.config.auth.handler.OAuth2MemberSuccessHandler;
import com.codestates.mainproject.oneyearfourcut.global.config.auth.jwt.JwtTokenizer;
import com.codestates.mainproject.oneyearfourcut.domain.refreshToken.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;

    private final JwtTokenizer jwtTokenizer;
    private final MemberService memberService;
    private final RefreshTokenService refreshTokenService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .headers().frameOptions().sameOrigin()
                .and()
                .csrf().disable()
                .cors(withDefaults())
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .formLogin().disable()
                .httpBasic().disable()
                .exceptionHandling()
                .authenticationEntryPoint(new MemberAuthenticationEntryPoint()) //????????? ??????????????? ????????? ????????? ???????????? ?????????
//                .accessDeniedHandler(new MemberAccessDeniedHandler()) // ????????? ??????????????? ?????? ???????????? ?????? ????????? ?????? ?????? ?????? -> ?????? ?????? ??????????
                .and()
                .apply(new CustomFilterConfigurer())
                .and()
                .authorizeHttpRequests(authorize -> authorize
                                .antMatchers(HttpMethod.GET, "/galleries/**").permitAll()
                                .antMatchers(HttpMethod.GET, "/auth/refresh/**").permitAll()
                                .antMatchers(HttpMethod.GET, "/docs/index.html").permitAll()
                                .antMatchers(HttpMethod.GET, "/").permitAll()
                                .antMatchers(HttpMethod.GET, "/receive-token").permitAll()
                                .antMatchers("/h2/**").permitAll()
                                .antMatchers("/ws/stomp/**").permitAll()
                                .antMatchers("/sub/**").permitAll()
                                .antMatchers("/pub/**").permitAll()
//                                .antMatchers(HttpMethod.GET, "/members/me/alarms/connect").permitAll()
//                                .antMatchers("/chats/**").permitAll()
                                .antMatchers(HttpMethod.GET, "/sse/**").permitAll()

//                                .antMatchers("/ws/**").permitAll() // -> websocket test
//                                .antMatchers("/members/**").hasRole("USER")
//                                .antMatchers("/galleries/**").hasRole("USER")
//                                .antMatchers(HttpMethod.DELETE, "/galleries/**").hasRole("USER")
                                .anyRequest().hasRole("USER")
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(new OAuth2MemberSuccessHandler(jwtTokenizer, memberService, refreshTokenService)));
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.addExposedHeader("Authorization");
        configuration.addExposedHeader("refresh");  //????????? ????????? ???????????? ?????? ???

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();   //  CorsConfigurationSource ?????????????????? ?????? ???????????? UrlBasedCorsConfigurationSource ???????????? ????????? ????????????.
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    public class CustomFilterConfigurer extends AbstractHttpConfigurer<CustomFilterConfigurer, HttpSecurity> {
        @Override
        public void configure(HttpSecurity builder) throws Exception {
            JwtVerificationFilter jwtVerificationFilter = new JwtVerificationFilter(jwtTokenizer);
            builder.addFilterAfter(jwtVerificationFilter, OAuth2LoginAuthenticationFilter.class);
        }
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        var clientRegistration = clientRegistration();

        return new InMemoryClientRegistrationRepository(clientRegistration);
    }

    private ClientRegistration clientRegistration() {
        return ThirdOAuth2Provider
                .KAKAO
                .getBuilder("kakao")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
    }
}
