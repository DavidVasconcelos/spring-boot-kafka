package com.udemy.kafka.config

import com.udemy.kafka.service.LibraryEventsService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.dao.RecoverableDataAccessException
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.retry.RetryPolicy
import org.springframework.retry.backoff.BackOffPolicy
import org.springframework.retry.backoff.FixedBackOffPolicy
import org.springframework.retry.policy.SimpleRetryPolicy
import org.springframework.retry.support.RetryTemplate

@Configuration
@EnableKafka
class LibraryEventsConsumerConfig {

    private val logger = LogManager.getLogger(javaClass)

    @Autowired
    private lateinit var libraryEventsService: LibraryEventsService

    @Bean
    fun kafkaListenerContainerFactory(configurer: ConcurrentKafkaListenerContainerFactoryConfigurer,
                                      kafkaConsumerFactory: ConsumerFactory<Any, Any>):
            ConcurrentKafkaListenerContainerFactory<Any, Any> {

        val factory = ConcurrentKafkaListenerContainerFactory<Any, Any>()
        configurer.configure(factory, kafkaConsumerFactory)

        //Vai separar 3 threads, como temos 3 partições, cada partição fica em uma diferente
        //Desnessário caso esteja rodando em varias instancias no Kubernetes por exemplo
        factory.setConcurrency(3)

        //Somente para o LibraryEventsConsumerManualOffset
        //factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL

        //error handler
        factory.setErrorHandler { thrownException, data ->
            logger.info("Exception in consumerConfig is ${thrownException.message} and the record is $data")
            //persistir os dados por exemplo se for conveniente
        }

        //retry
        factory.setRetryTemplate(retryTemplate())

        //recovery
        factory.setRecoveryCallback { context ->

            when (context.lastThrowable.cause) {
                is RecoverableDataAccessException -> {
                    logger.info("Inside the recoverable logic")

                    //para pegar o atributo certo do contexto
                    /*context.attributeNames().forEach { attributeName: String ->
                        logger.info("Attribute name is : $attributeName ")
                        logger.info("Attribute Value is : ${context.getAttribute(attributeName)} ")

                    }*/
                    val consumerRecord = context.getAttribute("record")
                            as ConsumerRecord<Int, String>
                    libraryEventsService.handleRecovery(consumerRecord)

                }
                else -> {
                    logger.info("Inside the non recoverable logic")
                    throw RuntimeException(context.lastThrowable.message)
                }
            }

            null

        }

        return factory
    }

    private fun retryTemplate(): RetryTemplate {

        val retryTemplate = RetryTemplate()
        retryTemplate.setRetryPolicy(simpleRetryPolicy())
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy())

        return retryTemplate
    }

    private fun simpleRetryPolicy(): RetryPolicy {

        //sem classificar a exception, ele faz o retry sempre
//        val simpleRetryPolicy = SimpleRetryPolicy()
//        simpleRetryPolicy.maxAttempts = 3

        //aqui faz o retry apenas se for RecoverableDataAccessException que no caso é falha na conexão com o banco
        val exceptionsMap = hashMapOf<Class<out Throwable>, Boolean>(
                IllegalArgumentException::class.java to false,
                RecoverableDataAccessException::class.java to true)

        return SimpleRetryPolicy(3, exceptionsMap, true)
    }

    private fun fixedBackOffPolicy(): BackOffPolicy {

        val fixedBackOffPolicy = FixedBackOffPolicy()
        fixedBackOffPolicy.backOffPeriod = 1000 //1 segundo

        return fixedBackOffPolicy
    }


}