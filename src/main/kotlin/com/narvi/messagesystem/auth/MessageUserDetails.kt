package com.narvi.messagesystem.auth

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class MessageUserDetails @JsonCreator constructor(
    @param:JsonProperty("userId") val userId: Long,
    @param:JsonProperty("username") private val username: String,
    @param:JsonProperty("password") private var password: String
) : UserDetails {

    @JsonIgnore // 이게 올바른 방법은 아닌데..
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

    fun erasePassword() {
        password = ""
    }
}