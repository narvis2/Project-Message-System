package com.narvi.messagesystem.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import com.narvi.messagesystem.dto.restapi.LoginRequest
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.util.matcher.RequestMatcher
import java.util.*

class RestApiLoginAuthFilter(
    requiresAuthenticationRequestMatcher: RequestMatcher,
    authenticationManager: AuthenticationManager,
) : AbstractAuthenticationProcessingFilter(requiresAuthenticationRequestMatcher, authenticationManager) {

    private val objectMapper = ObjectMapper().apply {
        registerModules(JavaTimeModule())
        registerModules(kotlinModule())
        registerModule(ParameterNamesModule())
        activateDefaultTyping(
            BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(Any::class.java)
                .build(),
            ObjectMapper.DefaultTyping.NON_FINAL,
        )
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    // 로그인 요청이 들어올때 호출됨
    // 인증 과정 요청
    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        if (!request.contentType.startsWith(MediaType.APPLICATION_JSON_VALUE)) {
            throw AuthenticationServiceException("지원하지 않는 타입 : ${request.contentType}")
        }

        val loginRequest = objectMapper.readValue(request.inputStream, LoginRequest::class.java)
        val authenticationToken = UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password)

        return authenticationManager.authenticate(authenticationToken)
    }

    override fun successfulAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
        authResult: Authentication
    ) {
        val securityContext = SecurityContextHolder.getContext()
        // Redis Session 에 password 가 저장되는 걸 방지
        (authResult.principal as? MessageUserDetails)?.erasePassword()
        securityContext.authentication = authResult

        val contextRepository = HttpSessionSecurityContextRepository()
        contextRepository.saveContext(securityContext, request, response)

        val sessionId = request.session.id
        val encodedSessionId = Base64.getEncoder()
            .encodeToString(sessionId.toByteArray(Charsets.UTF_8))

        response.status = HttpServletResponse.SC_OK
        response.contentType = MediaType.TEXT_PLAIN_VALUE
        response.characterEncoding = "UTF-8"
        response.writer.write(encodedSessionId)
        response.writer.flush()
    }

    override fun unsuccessfulAuthentication(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        failed: AuthenticationException?
    ) {
        response?.apply {
            status = HttpServletResponse.SC_UNAUTHORIZED
            characterEncoding = "UTF-8"
            contentType = MediaType.TEXT_PLAIN_VALUE
            writer.write("인증 실패")
            writer.flush()
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}