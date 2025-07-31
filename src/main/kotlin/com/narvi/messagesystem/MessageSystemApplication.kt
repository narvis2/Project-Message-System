package com.narvi.messagesystem

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing
@SpringBootApplication
class MessageSystemApplication

fun main(args: Array<String>) {
    runApplication<MessageSystemApplication>(*args)
}
