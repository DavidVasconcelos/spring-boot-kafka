package com.udemy.kafka.libraryeventsconsumer.consumer

import com.google.gson.Gson
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.logging.log4j.LogManager
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class LibraryEventsConsumer {

    private val logger = LogManager.getLogger(javaClass)

    @KafkaListener(topics = ["library-events"])
    fun onMessage(consumerRecord: ConsumerRecord<Int, String>) {

        logger.info("ConsumerRecord received: ${Gson().toJson(consumerRecord.value())}")

    }
}