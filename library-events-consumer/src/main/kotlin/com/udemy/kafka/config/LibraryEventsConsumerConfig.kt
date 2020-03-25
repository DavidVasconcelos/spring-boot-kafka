package com.udemy.kafka.config

import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.listener.ContainerProperties

@Configuration
@EnableKafka
class LibraryEventsConsumerConfig {

    @Bean
    fun kafkaListenerContainerFactory(configurer: ConcurrentKafkaListenerContainerFactoryConfigurer,
                                      kafkaConsumerFactory: ConsumerFactory<Any, Any>):
            ConcurrentKafkaListenerContainerFactory<Any, Any> {

        val factory = ConcurrentKafkaListenerContainerFactory<Any, Any>()
        configurer.configure(factory, kafkaConsumerFactory)

        //Vai separar 3 threads, como temos 3 partições, cada partição fica em uma diferente
        //Desnessário caso estaja rodando em varias instancias no Kubernetes por exemplo
        factory.setConcurrency(3)

        //Somente para o LibraryEventsConsumerManualOffset
        //factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL

        return factory
    }


}