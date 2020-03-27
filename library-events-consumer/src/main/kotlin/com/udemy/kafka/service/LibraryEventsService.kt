package com.udemy.kafka.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.udemy.kafka.entity.LibraryEvent
import com.udemy.kafka.entity.LibraryEventType
import com.udemy.kafka.repository.LibraryEventsRepository
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
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

        when (libraryEvent.libraryEventType) {
            LibraryEventType.NEW -> save(libraryEvent)
            LibraryEventType.UPDATE -> libraryEvent.apply { validate(this) }.also { save(it) }
            else -> logger.info("Invalid Library Event Type")

        }

    }

    private fun save(libraryEvent: LibraryEvent) {

        libraryEvent.book?.libraryEvent = libraryEvent
        libraryEventsRepository.save(libraryEvent)
        logger.info("Successfully Persisted the libary Event: $libraryEvent")
    }

    private fun validate(libraryEvent: LibraryEvent) {

        val id = libraryEvent.libraryEventId ?: throw IllegalArgumentException("Library Event Id is missing")
        libraryEventsRepository.findByIdOrNull(id) ?: throw IllegalArgumentException("Not a valid library Event")
        logger.info("Validation is successful for the library Event : $libraryEvent");
    }
}