package com.udemy.kafka.entity

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import java.io.Serializable
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToOne

@Entity
class LibraryEvent : Serializable {

    companion object {
        private const val serialVersionUID = -97059332L
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Expose(serialize = true)
    var libraryEventId: Int? = null

    @Enumerated
    @Expose(serialize = true)
    var libraryEventType: LibraryEventType? = null

    @OneToOne(mappedBy = "libraryEvent", cascade = [CascadeType.ALL])
    @Expose(serialize = false)
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
        return GsonBuilder()
                    .excludeFieldsWithoutExposeAnnotation()
                    .create()
                    .toJson(this)
    }
}