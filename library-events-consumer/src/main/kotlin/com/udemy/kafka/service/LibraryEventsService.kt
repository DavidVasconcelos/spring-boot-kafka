package com.udemy.kafka.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.udemy.kafka.entity.LibraryEvent
import com.udemy.kafka.entity.LibraryEventType
import com.udemy.kafka.repository.LibraryEventsRepository
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class LibraryEventsService {

    private val logger = LogManager.getLogger(javaClass)

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var libraryEventsRepository: LibraryEventsRepository

    fun processLibraryEvent(consumerRecord: ConsumerRecord<Int, String>) {

        val libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent::class.java)
        logger.info("LibraryEvent: $libraryEvent")

        when(libraryEvent.libraryEventType) {
            LibraryEventType.NEW -> save(libraryEvent)
            LibraryEventType.UPDATE -> Unit
            else -> logger.info("Invalid Library Event Type")

        }

    }

    private fun save(libraryEvent: LibraryEvent) {

        libraryEvent.book?.libraryEvent = libraryEvent
        libraryEventsRepository.save(libraryEvent)
        logger.info("Successfully Persisted the libary Event: $libraryEvent")



    }
}