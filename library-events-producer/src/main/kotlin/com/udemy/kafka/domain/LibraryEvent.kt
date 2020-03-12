package com.udemy.kafka.domain

import javax.validation.Valid
import javax.validation.constraints.NotNull

data class LibraryEvent(val libraryEventId: Int, var libraryEventType: LibraryEventType? = null,
                        @NotNull @Valid val book: Book)