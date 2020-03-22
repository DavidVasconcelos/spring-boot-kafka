package com.udemy.kafka.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.google.gson.Gson
import javax.validation.constraints.NotBlank

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Book {

    var bookId: Int? = null

    @NotBlank
    var bookName: String? = null

    @NotBlank
    var bookAuthor: String? = null

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
        return Gson().toJson(this)
    }

}

