package com.udemy.kafka.controller

import com.udemy.kafka.domain.Book
import com.udemy.kafka.domain.LibraryEvent
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.common.serialization.IntegerDeserializer
import org.apache.kafka.common.serialization.IntegerSerializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = ["library-events"], partitions = 3)
class LibraryEventsControllerIntegrationTest {

    @Value("\${spring.kafka.template.default-topic}")
    private lateinit var topic: String

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Autowired
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker

    private lateinit var consumer: Consumer<Int, String>

    @BeforeEach
    fun setUp() {

        val configs = KafkaTestUtils
                .consumerProps("group1", "true", embeddedKafkaBroker)

        consumer = DefaultKafkaConsumerFactory(configs, IntegerDeserializer(), StringDeserializer()).createConsumer()

        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer)

    }

    @AfterEach
    fun tearDown() {
        consumer.close()
    }

    @Test
    @Timeout(10)
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

        val httpHeaders = HttpHeaders()
        httpHeaders.set("content-type", MediaType.APPLICATION_JSON.toString())
        val request = HttpEntity<LibraryEvent>(libraryEvent, httpHeaders)

        //when
        val responseEntity = restTemplate.exchange("/v1/libraryevent", HttpMethod.POST,
                request, LibraryEvent::class.java)

        //then
        assertEquals(HttpStatus.CREATED, responseEntity.statusCode)

        val consumerRecord = KafkaTestUtils.getSingleRecord(consumer, topic)
        val expectedRecord = "{\"libraryEventId\":0,\"libraryEventType\":\"NEW\",\"book\":{\"bookId\":123," +
                "\"bookName\":\"Kafka using Spring Boot\",\"bookAuthor\":\"Dilip\"}}"
        val value = consumerRecord.value()

        assertEquals(expectedRecord, value)

    }

}