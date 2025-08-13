package com.narvi.messagesystem.service

import mu.KotlinLogging
import org.springframework.data.redis.core.RedisOperations
import org.springframework.data.redis.core.SessionCallback
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class CacheService(
    private val stringRedisTemplate: StringRedisTemplate,
) {

    fun get(key: String): String? = try {
        stringRedisTemplate.opsForValue().get(key)
    } catch (e: Exception) {
        log.error("Redis get failed. key: {}, cause: {}", key, e.message)
        null
    }

    fun get(keys: List<String>): List<String> = try {
        stringRedisTemplate.opsForValue().multiGet(keys) ?: emptyList()
    } catch (e: Exception) {
        log.error("Redis multi get failed. keys: {}, cause: {}", keys, e.message)
        emptyList()
    }

    fun set(key: String, value: String, ttlSeconds: Long): Boolean = try {
        stringRedisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS)
        true
    } catch (e: Exception) {
        log.error("Redis set failed. key: {}, cause: {}", key, e.message)
        false
    }

    fun set(map: Map<String, String>, ttlSeconds: Long): Boolean = runCatching {
        stringRedisTemplate.executePipelined(object : SessionCallback<Void?> {
            override fun <K : Any?, V : Any?> execute(operations: RedisOperations<K, V>): Void? {
                val stringOps = operations as RedisOperations<String, String>
                map.forEach { (key, value) ->
                    stringOps.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS)
                }

                return null
            }
        })
        true
    }.getOrElse {
        log.error("Redis multiset failed. key: {}, cause: {}", map.keys, it.message)
        false
    }

    fun expire(key: String, ttlSeconds: Long): Boolean = try {
        stringRedisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS)
    } catch (e: Exception) {
        log.error("Redis expire failed. key: {}, cause: {}", key, e.message)
        false
    }

    fun delete(key: String): Boolean = try {
        stringRedisTemplate.delete(key)
        true
    } catch (e: Exception) {
        log.error("Redis delete failed. key: {}, cause: {}", key, e.message)
        false
    }

    fun delete(keys: List<String>): Boolean = try {
        stringRedisTemplate.delete(keys)
        true
    } catch (e: Exception) {
        log.error("Redis multi delete failed. keys: {}, cause: {}", keys, e.message)
        false
    }

    fun buildKey(prefix: String, key: String): String = "%s:%s".format(prefix, key)

    fun buildKey(prefix: String, firstKey: String, secondKey: String): String =
        "%s:%s:%s".format(prefix, firstKey, secondKey)

    companion object {
        private val log = KotlinLogging.logger {}
    }
}