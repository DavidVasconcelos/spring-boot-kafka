package com.udemy.kafka.controller

import com.udemy.kafka.domain.LibraryEvent
import com.udemy.kafka.domain.LibraryEventType
import com.udemy.kafka.producer.LibraryEventProducer
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
class LibraryEventsController {

    private val logger = LogManager.getLogger(javaClass)

    @Autowired
    private lateinit var libraryEventProducer: LibraryEventProducer

    @PostMapping("/v1/libraryevent")
    fun postLibraryEvent(@RequestBody @Valid libraryEvent: LibraryEvent) : ResponseEntity<LibraryEvent> {

        libraryEvent.run { libraryEventType = LibraryEventType.NEW }
        logger.info("Post message -> $libraryEvent")
        libraryEventProducer.sendLibraryEvent_Approach2(libraryEvent)

        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }
}