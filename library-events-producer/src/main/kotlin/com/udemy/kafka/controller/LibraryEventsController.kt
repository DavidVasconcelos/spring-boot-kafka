package com.udemy.kafka.controller

import com.udemy.kafka.domain.LibraryEvent
import com.udemy.kafka.domain.LibraryEventType
import com.udemy.kafka.producer.LibraryEventsProducer
import com.udemy.kafka.util.ID_NULL_ON_UPDATE_MESSAGE_ERROR
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
class LibraryEventsController {

    private val logger = LogManager.getLogger(javaClass)

    @Autowired
    private lateinit var libraryEventsProducer: LibraryEventsProducer

    @PostMapping("/v1/libraryevent")
    fun postLibraryEvent(@RequestBody @Valid libraryEvent: LibraryEvent) : ResponseEntity<LibraryEvent> {

        libraryEvent.run { libraryEventType = LibraryEventType.NEW }
        logger.info("Post message -> $libraryEvent")
        libraryEventsProducer.sendLibraryEvent_Approach2(libraryEvent)

        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent)
    }

    @PutMapping("/v1/libraryevent")
    fun putLibraryEvent(@RequestBody @Valid libraryEvent: LibraryEvent) : ResponseEntity<*> {

        libraryEvent.libraryEventId ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ID_NULL_ON_UPDATE_MESSAGE_ERROR)

        libraryEvent.run { libraryEventType = LibraryEventType.UPDATE }
        logger.info("Put message -> $libraryEvent")
        libraryEventsProducer.sendLibraryEvent_Approach2(libraryEvent)

        return ResponseEntity.status(HttpStatus.OK).body(libraryEvent)
    }
}