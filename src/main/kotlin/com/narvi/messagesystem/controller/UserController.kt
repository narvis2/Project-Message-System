package com.narvi.messagesystem.controller

import com.narvi.messagesystem.dto.restapi.UserRegisterRequest
import com.narvi.messagesystem.service.UserService
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class UserController(
    private val userService: UserService,
) {

    @PostMapping("/register")
    fun register(
        @RequestBody request: UserRegisterRequest
    ): ResponseEntity<String> =
        try {
            userService.addUser(request.username, request.password)
            ResponseEntity.ok("User registered.")
        } catch (ex: Exception) {
            log.error("Add user failed. cause: {}", ex.message)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Register user failed")
        }

    @PostMapping("/unregister")
    fun unregister(request: HttpServletRequest): ResponseEntity<String> = try {
        userService.removeUser()
        request.session.invalidate()
        ResponseEntity.ok("User unregistered.")
    } catch (ex: Exception) {
        log.error("Remove user failed. cause: {}", ex.message)
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unregister user failed")
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}