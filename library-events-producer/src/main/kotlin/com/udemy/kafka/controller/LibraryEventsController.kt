package com.udemy.kafka.controller

import com.udemy.kafka.domain.LibraryEvent
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
class LibraryEventsController {

    @PostMapping("/v1/libraryevent")
    fun postLibraryEvent(@RequestBody @Valid libraryEvent: LibraryEvent) : ResponseEntity<LibraryEvent> {


        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }
}