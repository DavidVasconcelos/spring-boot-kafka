package com.udemy.kafka.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.google.gson.Gson
import javax.validation.Valid
import javax.validation.constraints.NotNull

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class LibraryEvent {

    var libraryEventId: Int? = null

    var libraryEventType: LibraryEventType? = null

    @NotNull
    @Valid
    var book: Book? = null

    private constructor(libraryEventId: Int?, libraryEventType: LibraryEventType?, book: Book?) {
        this.libraryEventId = libraryEventId
        this.libraryEventType = libraryEventType
        this.book = book
    }

    constructor()

    data class Builder(
            var libraryEventId: Int? = null,
            var libraryEventType: LibraryEventType? = null,
            var book: Book? = null) {

        fun libraryEventId(libraryEventId: Int) = apply { this.libraryEventId = libraryEventId }
        fun libraryEventType(libraryEventType: LibraryEventType) = apply { this.libraryEventType = libraryEventType }
        fun book(book: Book) = apply { this.book = book }
        fun build() = LibraryEvent(libraryEventId, libraryEventType, book)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LibraryEvent) return false

        if (libraryEventId != other.libraryEventId) return false
        if (libraryEventType != other.libraryEventType) return false
        if (book != other.book) return false

        return true
    }

    override fun hashCode(): Int {
        var result = libraryEventId ?: 0
        result = 31 * result + (libraryEventType?.hashCode() ?: 0)
        result = 31 * result + (book?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return Gson().toJson(this)
    }

}