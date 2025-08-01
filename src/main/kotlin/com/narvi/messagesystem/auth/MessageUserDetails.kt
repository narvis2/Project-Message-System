package com.narvi.messagesystem.auth

import com.fasterxml.jackson.annotation.JsonCreator
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class MessageUserDetails @JsonCreator constructor(
    private val userId: Long,
    private val username: String,
    private var password: String,
) : UserDetails {

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = arrayListOf()

    override fun getPassword(): String = password

    override fun getUsername(): String = username

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageUserDetails

        return username == other.username
    }

    override fun hashCode(): Int {
        return username.hashCode()
    }

    val getUserId
        get() = userId

    fun erasePassword() {
        password = ""
    }
}