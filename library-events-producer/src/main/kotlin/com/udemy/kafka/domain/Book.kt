package com.udemy.kafka.domain

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class Book private constructor(@NotNull val bookId: Int, @NotBlank val bookName: String,
                               @NotBlank val bookAuthor: String) {

    data class Builder(
            var bookId: Int? = null,
            var bookName: String? = null,
            var bookAuthor: String? = null) {

        fun bookId(bookId: Int) = apply { this.bookId = bookId }
        fun bookName(bookName: String) = apply { this.bookName = bookName }
        fun bookAuthor(bookAuthor: String) = apply { this.bookAuthor = bookAuthor }
        fun build() = Book(bookId!!, bookName!!, bookAuthor!!)
    }
}

