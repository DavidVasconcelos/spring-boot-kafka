package com.udemy.kafka.domain

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class Book(@NotNull val bookId: Int, @NotBlank val bookName: String, @NotBlank val bookAuthor: String)