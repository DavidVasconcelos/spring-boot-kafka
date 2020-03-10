package com.udemy.kafka.controller

import com.udemy.kafka.domain.LibraryEvent
import com.udemy.kafka.producer.LibraryEventProducer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
class LibraryEventsController {

    @Autowired
    private lateinit var libraryEventProducer: LibraryEventProducer

    @PostMapping("/v1/libraryevent")
    fun postLibraryEvent(@RequestBody @Valid libraryEvent: LibraryEvent) : ResponseEntity<LibraryEvent> {

        libraryEventProducer.sendLibraryEvent(libraryEvent)

        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }
}