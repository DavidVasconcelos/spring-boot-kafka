package com.udemy.kafka

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LibraryEventsProducerApplication

fun main(args: Array<String>) {
	runApplication<LibraryEventsProducerApplication>(*args)
}
