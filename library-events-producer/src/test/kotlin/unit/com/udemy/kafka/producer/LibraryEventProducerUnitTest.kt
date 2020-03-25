package com.udemy.kafka.producer

import com.fasterxml.jackson.databind.ObjectMapper
import com.udemy.kafka.domain.Book
import com.udemy.kafka.domain.LibraryEvent
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.OverrideMockKs
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.util.concurrent.SettableListenableFuture

@ExtendWith(MockKExtension::class)
class LibraryEventProducerUnitTest {

    @MockK
    private lateinit var kafkaTemplate: KafkaTemplate<Int, String>

    @OverrideMockKs
    var eventProducer: LibraryEventProducer = LibraryEventProducer()

    @SpyK
    private var objectMapper = ObjectMapper()

    @Test
    fun sendLibraryEvent_Approach2_failure() {

        //given
        val book = Book.Builder()
                .bookId(123)
                .bookAuthor("Dilip")
                .bookName("Kafka using Spring Boot")
                .build()

        val libraryEvent = LibraryEvent.Builder()
                .book(book)
                .build()

        val future = SettableListenableFuture<Any>()
                as SettableListenableFuture<SendResult<Int, String>>
        future.setException(RuntimeException("Exception Calling Kafka"))

        every { kafkaTemplate.send(any<ProducerRecord<Int, String>>()) } returns future

        //when
        //then
        assertThrows<Exception> {
            eventProducer.sendLibraryEvent_Approach2(libraryEvent).get()
        }

    }

    @Test
    fun sendLibraryEvent_Approach2_sucess() {

        //given
        val book = Book.Builder()
                .bookId(123)
                .bookAuthor("Dilip")
                .bookName("Kafka using Spring Boot")
                .build()

        val libraryEvent = LibraryEvent.Builder()
                .book(book)
                .build()

        val record = objectMapper.writeValueAsString(libraryEvent)
        val future = SettableListenableFuture<Any>()
                as SettableListenableFuture<SendResult<Int, String>>

        val producerRecord = ProducerRecord<Int, String>("library-events", libraryEvent.libraryEventId, record)
        val recordMetadata = RecordMetadata(TopicPartition("library-events", 1),
                1, 1, 342, System.currentTimeMillis(), 1, 2)

        val sendResult = SendResult(producerRecord, recordMetadata)
        future.set(sendResult)

        every { kafkaTemplate.send(any<ProducerRecord<Int, String>>()) } returns future

        //when
        val sendResultReturned = eventProducer.sendLibraryEvent_Approach2(libraryEvent).get()

        //then
        assertEquals(sendResultReturned.recordMetadata.partition(), 1)

    }
}