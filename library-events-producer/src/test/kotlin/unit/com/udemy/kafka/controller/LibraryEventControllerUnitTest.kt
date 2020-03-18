package com.udemy.kafka.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.udemy.kafka.domain.Book
import com.udemy.kafka.domain.LibraryEvent
import com.udemy.kafka.producer.LibraryEventProducer
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(LibraryEventsController::class)
@AutoConfigureMockMvc
class LibraryEventControllerUnitTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var libraryEventProducer: LibraryEventProducer

    val objectMapper = ObjectMapper()

    @Test
    fun postLibraryEvent() {

        //given
        val book = Book.Builder()
                .bookId(123)
                .bookAuthor("Dilip")
                .bookName("Kafka using Spring Boot")
                .build()

        val libraryEvent = LibraryEvent.Builder()
                .libraryEventId(0)
                .book(book)
                .build()

        val json = objectMapper.writeValueAsString(libraryEvent)

       every { libraryEventProducer.sendLibraryEvent_Approach2(any()).get() } returns null

        //expect
        mockMvc.post("/v1/libraryevent") {
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
            content = json

        }.andExpect {
            status { isCreated }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    @Test
    fun postLibraryEvent_4xx() {

        //given
        val book: Book = Book.Builder()
                .bookName("Kafka using Spring Boot")
                .build()

       val libraryEvent = LibraryEvent.Builder()
                .libraryEventId(0)
                .book(book)
                .build()

        val json = objectMapper.writeValueAsString(libraryEvent)

        every { libraryEventProducer.sendLibraryEvent_Approach2(any()).get() } returns null

        //expect
        mockMvc.post("/v1/libraryevent") {
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
            content = json

        }.andExpect {
            status { is4xxClientError }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { string("book.bookAuthor - must not be blank, book.bookId - must not be null") }
        }
    }
}