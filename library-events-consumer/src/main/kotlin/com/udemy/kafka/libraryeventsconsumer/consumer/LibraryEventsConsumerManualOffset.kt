package com.udemy.kafka.libraryeventsconsumer.consumer

import com.google.gson.Gson
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.logging.log4j.LogManager
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.AcknowledgingMessageListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

//@Component
class LibraryEventsConsumerManualOffset : AcknowledgingMessageListener<Int, String> {

    private val logger = LogManager.getLogger(javaClass)

    @KafkaListener(topics = ["library-events"])
    override fun onMessage(consumerRecord: ConsumerRecord<Int, String>, acknowledgment: Acknowledgment) {

        logger.info("ConsumerRecord received: ${Gson().toJson(consumerRecord.value())}")
        acknowledgment.acknowledge()
    }
}