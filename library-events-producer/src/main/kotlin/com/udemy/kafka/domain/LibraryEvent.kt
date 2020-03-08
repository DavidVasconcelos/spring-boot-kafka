package com.udemy.kafka.domain

import javax.validation.Valid
import javax.validation.constraints.NotNull

data class LibraryEvent(val libraryEventId: Int, val libraryEventType: LibraryEventType, @NotNull @Valid val book: Book)