package com.udemy.kafka.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.kafka.config.TopicBuilder

@Configuration
@Profile("local")
class AutoCreateConfig {

    @Value("\${spring.kafka.template.default-topic}")
    private lateinit var topic: String

    @Bean
    fun libraryEvents(): NewTopic {

        return TopicBuilder.name(topic)
                .partitions(3)
                .replicas(3)
                .build();

    }
}