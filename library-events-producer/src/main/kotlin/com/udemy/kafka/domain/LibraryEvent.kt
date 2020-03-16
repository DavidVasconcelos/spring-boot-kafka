package com.udemy.kafka.domain

import javax.validation.Valid
import javax.validation.constraints.NotNull

data class LibraryEvent(val libraryEventId: Int, var libraryEventType: LibraryEventType? = null,
                        @NotNull @Valid val book: Book) {

    data class Builder(
            var libraryEventId: Int? = null,
            var libraryEventType: LibraryEventType? = null,
            var book: Book? = null) {

        fun libraryEventId(libraryEventId: Int) = apply { this.libraryEventId = libraryEventId }
        fun libraryEventType(libraryEventType: LibraryEventType?) = apply { this.libraryEventType = libraryEventType }
        fun book(book: Book) = apply { this.book = book }
        fun build() = LibraryEvent(libraryEventId!!, libraryEventType, book!!)
    }
}