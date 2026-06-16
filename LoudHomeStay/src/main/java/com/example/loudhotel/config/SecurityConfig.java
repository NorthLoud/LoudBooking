package com.example.loudhotel.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .cors(cors -> {}) // ✅ bật CORS
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // ✅ JWT stateless
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // ✅ FIX 403 OPTIONS
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/chatbot/**").permitAll()
                        .requestMatchers("/css/chatbot.css", "/js/chatbot.js").permitAll()
                        .requestMatchers("/api/payments/**").permitAll()
                        .requestMatchers("/api/sse/**").authenticated()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/images/**").permitAll()
                        .requestMatchers("/api/chat/**").authenticated()
                        .requestMatchers("/chat-socket/**").permitAll()
                        .requestMatchers(
                                "/",
                                "/*.html",
                                "/user/**",
                                "/admin/**",
                                "/manager/**",
                                "/images/**"
                        ).permitAll()

                        //hotel
                        // PUBLIC
                        .requestMatchers(HttpMethod.GET, "/api/hotels/**").permitAll()
                        // PROTECTED
                        .requestMatchers(HttpMethod.DELETE, "/api/hotels/images/**")
                        .hasAnyRole("ADMIN","MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/hotels").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/hotels/**").hasAnyRole("ADMIN","MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/hotels/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/hotels/*/approve").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/hotels/*/reject").hasRole("ADMIN")

                        //Room
                        .requestMatchers(HttpMethod.POST,"/api/rooms/hotel/*")
                        .hasAnyRole("ADMIN","MANAGER")
                        // admin
                        .requestMatchers(HttpMethod.PUT, "/api/rooms/*/block").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/rooms/*/unblock").hasRole("ADMIN")
                        /* MANAGER only */
                        .requestMatchers(HttpMethod.GET,"/api/rooms/my").hasRole("MANAGER")
                        /* room cho cả admin + manager */
                        .requestMatchers(HttpMethod.DELETE,"/api/rooms/*")
                        .hasAnyRole("ADMIN","MANAGER")
                        .requestMatchers(HttpMethod.POST,"/api/rooms/*/images")
                        .hasAnyRole("ADMIN","MANAGER")
                        .requestMatchers(HttpMethod.PUT,"/api/rooms/*")
                        .hasAnyRole("ADMIN","MANAGER")
                        .requestMatchers(HttpMethod.DELETE,"/api/rooms/images/*")
                        .hasAnyRole("ADMIN","MANAGER")
                        /* public xem room */
                        .requestMatchers(HttpMethod.GET,"/api/rooms/**")
                        .permitAll()

                        // ===== RoomType =====
                        .requestMatchers(HttpMethod.POST, "/api/room-types/**").hasAnyRole("ADMIN","MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/room-types/**").hasAnyRole("ADMIN","MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/room-types/**").hasAnyRole("ADMIN","MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/room-types/**").permitAll()

                        // Voucher
                        .requestMatchers(HttpMethod.GET, "/api/vouchers/public").permitAll()
                        // manager xem voucher
                        .requestMatchers(HttpMethod.GET, "/api/vouchers/manager")
                        .hasRole("MANAGER")
                        // admin xem tất cả
                        .requestMatchers(HttpMethod.GET, "/api/vouchers/**")
                        .hasRole("ADMIN")
                        // chỉ admin được CRUD
                        .requestMatchers(HttpMethod.POST, "/api/vouchers")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/vouchers/*")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/vouchers/*")
                        .hasRole("ADMIN")

                        // ===== Utilities tổng =====
                        .requestMatchers(HttpMethod.GET,"/api/utilities/**")
                        .hasAnyRole("ADMIN","MANAGER")
                        .requestMatchers(HttpMethod.POST,"/api/utilities/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,"/api/utilities/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,"/api/utilities/**")
                        .hasRole("ADMIN")

                        // ===== Utilities theo hotel =====
                        // MANAGER
                        .requestMatchers("/api/manager/**").hasRole("MANAGER")

                        // ===== Utilities theo RoomType =====
                        .requestMatchers("/api/manager/room-type-utilities/**").hasRole("MANAGER")
                        .requestMatchers("/api/admin/room-type-utilities/**").hasRole("ADMIN")
                        .requestMatchers("/api/utilities-room-types/**").permitAll()


                        //Đánh giá
                        .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()
                        .requestMatchers("/api/reviews/**").authenticated()

                        // ===== BILL =====
                        // user xem đơn của mình
                        .requestMatchers(HttpMethod.GET,"/api/bills/my")
                        .authenticated()
                        // MANAGER xem bill hotel của mình
                        .requestMatchers(HttpMethod.GET,"/api/bills/manager")
                        .hasRole("MANAGER")
                        // ADMIN xem tất cả
                        .requestMatchers(HttpMethod.GET,"/api/bills").hasAnyRole("ADMIN","MANAGER")
                        .requestMatchers(HttpMethod.GET,"/api/bills/*").hasAnyRole("USER","MANAGER","ADMIN")
                        // tạo đơn
                        .requestMatchers(HttpMethod.POST,"/api/bills").authenticated()
                        // MANAGER thao tác đơn thuộc hotel của mình
                        .requestMatchers(HttpMethod.PUT,"/api/bills/*/pay").hasAnyRole("ADMIN","MANAGER")
                        .requestMatchers(HttpMethod.PUT,"/api/bills/*/check-in").hasAnyRole("ADMIN","MANAGER")
                        .requestMatchers(HttpMethod.PUT,"/api/bills/*/check-out").hasAnyRole("ADMIN","MANAGER")
                        .requestMatchers(HttpMethod.PUT,"/api/bills/*/confirm-extra-fee").hasAnyRole("ADMIN","MANAGER")
                        .requestMatchers(HttpMethod.PUT,"/api/bills/*/cancel")
                        .hasAnyRole("MANAGER","USER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT,"/api/bills/*/hold").hasAnyRole("USER","MANAGER")

                        // admin không được thao tác trạng thái
                        // User sửa tài khoản
                        .requestMatchers("/api/users/me")
                        .authenticated()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
