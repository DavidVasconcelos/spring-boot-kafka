package com.udemy.kafka.entity

import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import java.io.Serializable
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne


@Entity
class Book : Serializable {

    companion object {
        private const val serialVersionUID = -90006283L
    }

    @Id
    @Expose(serialize = true)
    var bookId: Int? = null

    @Expose(serialize = true)
    var bookName: String? = null

    @Expose(serialize = true)
    var bookAuthor: String? = null

    @OneToOne
    @JoinColumn(name = "libraryEventId")
    @Expose(serialize = false)
    var libraryEvent: LibraryEvent? = null

    private constructor(bookId: Int?, bookName: String?, bookAuthor: String?) {
        this.bookId = bookId
        this.bookName = bookName
        this.bookAuthor = bookAuthor
    }

    constructor()

    data class Builder(
            var bookId: Int? = null,
            var bookName: String? = null,
            var bookAuthor: String? = null) {

        fun bookId(bookId: Int) = apply { this.bookId = bookId }
        fun bookName(bookName: String) = apply { this.bookName = bookName }
        fun bookAuthor(bookAuthor: String) = apply { this.bookAuthor = bookAuthor }
        fun build() = Book(bookId, bookName, bookAuthor)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Book) return false

        if (bookId != other.bookId) return false
        if (bookName != other.bookName) return false
        if (bookAuthor != other.bookAuthor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bookId ?: 0
        result = 31 * result + (bookName?.hashCode() ?: 0)
        result = 31 * result + (bookAuthor?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create()
                .toJson(this)
    }
}

