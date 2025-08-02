package com.narvi.messagesystem.config

import com.narvi.messagesystem.auth.RestApiLoginAuthFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher
import java.io.IOException

@Configuration
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(
        messageUserDetailsService: UserDetailsService,
        passwordEncoder: PasswordEncoder
    ): AuthenticationManager {
        val daoAuthenticationProvider = DaoAuthenticationProvider(messageUserDetailsService).apply {
            setPasswordEncoder(passwordEncoder)
        }

        return ProviderManager(daoAuthenticationProvider)
    }

    @Bean
    fun securityFilterChain(
        httpSecurity: HttpSecurity,
        authenticationManager: AuthenticationManager
    ): SecurityFilterChain {
        val loginRequestMatcher = PathPatternRequestMatcher
            .withDefaults()
            .matcher(HttpMethod.POST, "/api/v1/auth/login")

        val restApiLoginAuthFilter = RestApiLoginAuthFilter(
            loginRequestMatcher, authenticationManager
        )

        httpSecurity.csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .addFilterAt(restApiLoginAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(
                    "/api/v1/auth/register",
                    "/api/v1/auth/login",
                ).permitAll().anyRequest().authenticated()
            }
            .logout {
                it.logoutUrl("/api/v1/auth/logout").logoutSuccessHandler(this::logoutHandler)
            }

        return httpSecurity.build()
    }

    private fun logoutHandler(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication?
    ) {
        response.contentType = MediaType.TEXT_PLAIN_VALUE
        response.characterEncoding = "UTF-8"

        val message = if (authentication != null && authentication.isAuthenticated) {
            response.status = HttpStatus.OK.value()
            "로그아웃 성공"
        } else {
            response.status = HttpStatus.UNAUTHORIZED.value()
            "로그아웃 실패"
        }

        try {
            response.writer.write(message)
        } catch (e: IOException) {
            log.info("전송 실패. 원인 : {}", e.message)
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}