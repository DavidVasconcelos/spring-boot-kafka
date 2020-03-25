package com.udemy.kafka.repository

import com.udemy.kafka.entity.LibraryEvent
import org.springframework.data.repository.CrudRepository

interface LibraryEventsRepository : CrudRepository<LibraryEvent, Int>