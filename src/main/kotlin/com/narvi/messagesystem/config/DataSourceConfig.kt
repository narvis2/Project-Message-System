package com.narvi.messagesystem.config

import com.narvi.messagesystem.database.RoutingDataSource
import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import javax.sql.DataSource

@Configuration
class DataSourceConfig {

    // 메인(쓰기) 데이터베이스 커넥션 풀을 생성
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.source.hikari")
    fun sourceDataSource(): DataSource = DataSourceBuilder.create().type(HikariDataSource::class.java).build()

    // 읽기 전용(리플리카) 데이터베이스 커넥션 풀을 생성
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.replica.hikari")
    fun replicaDataSource(): DataSource = DataSourceBuilder.create().type(HikariDataSource::class.java).build()

    // 요청의 목적(읽기/쓰기)에 따라 sourceDataSource 또는 replicaDataSource 를 자동으로 선택해 주는 라우팅 DataSource 를 제공
    @Bean
    fun routingDataSource(
        @Qualifier("sourceDataSource") sourceDataSource: DataSource,
        @Qualifier("replicaDataSource") replicaDataSource: DataSource,
    ): DataSource {
        val routingDataSource = RoutingDataSource()
        val targetDataSources: MutableMap<Any, Any> = HashMap()

        targetDataSources["source"] = sourceDataSource
        targetDataSources["replica"] = replicaDataSource

        routingDataSource.setTargetDataSources(targetDataSources)
        routingDataSource.setDefaultTargetDataSource(sourceDataSource)

        replicaDataSource.connection.use {
            log.info("Init ReplicaConnectionPool.")
        }

        return routingDataSource
    }

    /**
     * 실제로 커넥션이 필요할 때까지(DataSourceProxy 레벨에서) DB 커넥션 획득을 지연시켜주는 프록시를 제공
     * ex) 트랜잭션 없이 단순히 빈을 참조만 해도 커넥션을 당장 열지 않고, 실제 쿼리 실행 시점에 연결을 맺음
     */
    @Primary
    @Bean
    fun lazyConnectionDataSource(
        @Qualifier("routingDataSource") routingDataSource: DataSource
    ): DataSource = LazyConnectionDataSourceProxy(routingDataSource)

    companion object {
        private val log = KotlinLogging.logger {}
    }
}