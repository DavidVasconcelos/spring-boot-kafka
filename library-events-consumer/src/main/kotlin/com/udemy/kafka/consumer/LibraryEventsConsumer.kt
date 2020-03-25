package com.udemy.kafka.consumer

import com.google.gson.Gson
import com.udemy.kafka.service.LibraryEventsService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class LibraryEventsConsumer {

    private val logger = LogManager.getLogger(javaClass)

    @Autowired
    private lateinit var libraryEventsService: LibraryEventsService

    @KafkaListener(topics = ["library-events"])
    fun onMessage(consumerRecord: ConsumerRecord<Int, String>) {

        logger.info("ConsumerRecord received: ${Gson().toJson(consumerRecord.value())}")
        libraryEventsService.processLibraryEvent(consumerRecord)

    }
}