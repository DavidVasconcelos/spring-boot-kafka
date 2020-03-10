package com.udemy.kafka.producer

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.udemy.kafka.domain.LibraryEvent
import org.apache.logging.log4j.LogManager.getLogger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component
import org.springframework.util.concurrent.ListenableFutureCallback
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException


@Component
class LibraryEventProducer {

    private val logger = getLogger(javaClass)

    @Value("\${spring.kafka.template.default-topic}")
    private lateinit var topic: String

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<Int, String>

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Throws(JsonProcessingException::class, ExecutionException::class, InterruptedException::class,
            TimeoutException::class)
    fun sendLibraryEvent(libraryEvent: LibraryEvent) {

        val key = libraryEvent.libraryEventId
        val value = objectMapper.writeValueAsString(libraryEvent)

        val listenableFuture = kafkaTemplate
                .sendDefault(key, value)

        listenableFuture.addCallback(object : ListenableFutureCallback<SendResult<Int, String>> {

            override fun onFailure(ex: Throwable) {
                handleFailure(ex)
            }

            override fun onSuccess(result: SendResult<Int, String>?) {
                handleSuccess(key, value, result)
            }


        })
    }

    private fun handleFailure(ex: Throwable) {

        logger.error("Error Sending the Message and the exception is ${ex.message}")

        try {
            throw ex
        } catch (throwable: Throwable) {
            logger.error("Error in OnFailure: ${throwable.message}")
        }

    }

    private fun handleSuccess(key: Int, value: String?, result: SendResult<Int, String>?) {

        logger.info("Message Sent SuccessFully for the key : $key and the value is $value , " +
                "partition is ${result?.recordMetadata?.partition()}")
    }
}



