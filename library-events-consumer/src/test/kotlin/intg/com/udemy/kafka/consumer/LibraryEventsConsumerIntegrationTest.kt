package com.udemy.kafka.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.SpykBean
import com.udemy.kafka.entity.Book
import com.udemy.kafka.entity.LibraryEvent
import com.udemy.kafka.entity.LibraryEventType
import com.udemy.kafka.repository.LibraryEventsRepository
import com.udemy.kafka.service.LibraryEventsService
import io.mockk.verify
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


@SpringBootTest
@EmbeddedKafka(topics = ["library-events"], partitions = 3)
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
class LibraryEventsConsumerIntegrationTest {

    @Autowired
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<Int, String>

    @Autowired
    private lateinit var endpointRegistry: KafkaListenerEndpointRegistry

    @SpykBean
    private lateinit var libraryEventsConsumerSpy: LibraryEventsConsumer

    @SpykBean
    private lateinit var libraryEventsServiceSpy: LibraryEventsService

    @Autowired
    private lateinit var libraryEventsRepository: LibraryEventsRepository

    val objectMapper = ObjectMapper()

    @Container
    private val container: MySQLContainer<Nothing> = MySQLContainer<Nothing>("mysql:5.7")
            .apply {
                withExposedPorts(3306)
                withPassword("MySql2020!")
                withDatabaseName("kafkadb")
                withInitScript("kafkadb.sql")
                waitingFor(Wait.forListeningPort())
                start()
                System.setProperty("DB_URL", this.jdbcUrl)
                System.setProperty("DB_USERNAME", this.username)
                System.setProperty("DB_PASSWORD", this.password)
            }


    @BeforeAll
    fun setUp() {

        for (messageListenerContainer in endpointRegistry.listenerContainers) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafkaBroker.partitionsPerTopic)
        }

    }

    @AfterAll
    fun tearDown() {
        embeddedKafkaBroker.kafkaServers.forEach { b -> b.shutdown() }
        embeddedKafkaBroker.kafkaServers.forEach { b -> b.awaitShutdown() }
    }

    @AfterEach
    fun cleanDataBase() {
        libraryEventsRepository.deleteAll()
    }

    @Test
    fun publishNewLibraryEvent() {

        //given
        val json = "{\"libraryEventId\":null,\"libraryEventType\":\"NEW\",\"book\":{\"bookId\":456," +
                "\"bookName\":\"Kafka Using Spring Boot\",\"bookAuthor\":\"Dilip\"}}"

        kafkaTemplate.sendDefault(json).get()

        //when
        var latch = CountDownLatch(1)
        latch.await(3, TimeUnit.SECONDS)

        //then
        verify(exactly = 1) { libraryEventsConsumerSpy.onMessage(any()) }
        verify(exactly = 1) { libraryEventsServiceSpy.processLibraryEvent(any()) }

        val libraryEventList = libraryEventsRepository.findAll()
        assert(libraryEventList.count() == 1)
        libraryEventList.forEach { libraryEvent: LibraryEvent? ->
            libraryEvent?.libraryEventId ?: fail(message = "libraryEventId was null")
            assertEquals(456, libraryEvent.book?.bookId)
        }
    }

    @Test
    fun publishUpdateLibraryEvent() {

        //given
        val json = "{\"libraryEventId\":null,\"libraryEventType\":\"NEW\",\"book\":{\"bookId\":456," +
                "\"bookName\":\"Kafka Using Spring Boot\",\"bookAuthor\":\"Dilip\"}}"

        val libraryEvent = objectMapper.readValue(json, LibraryEvent::class.java)
        libraryEvent.book?.libraryEvent = libraryEvent
        libraryEventsRepository.save(libraryEvent)

        //publish the update LibraryEvent
        val updatedBook = Book.Builder()
                .bookId(456)
                .bookName("Kafka Using Spring Boot 2.x")
                .bookAuthor("Dilip")
                .build()

        libraryEvent.libraryEventType = LibraryEventType.UPDATE
        libraryEvent.book = updatedBook

        val updatedJson = objectMapper.writeValueAsString(libraryEvent)

        libraryEvent.libraryEventId?.let {
            kafkaTemplate.sendDefault(it, updatedJson)
        }

        //when
        var latch = CountDownLatch(1)
        latch.await(3, TimeUnit.SECONDS)


        //then
        verify(exactly = 1) { libraryEventsConsumerSpy.onMessage(any()) }
        verify(exactly = 1) { libraryEventsServiceSpy.processLibraryEvent(any()) }

        libraryEvent.libraryEventId?.let {
            val persistedLibraryEvent = libraryEventsRepository.findById(it).get()

            assertEquals("Kafka Using Spring Boot 2.x", persistedLibraryEvent.book?.bookName)

        }

    }

    @Test
    fun publishModifyLibraryEventNotAValidLibraryEventId() {

        //given
        val libraryEventId = 123
        val json = "{\"libraryEventId\":$libraryEventId,\"libraryEventType\":\"UPDATE\",\"book\":{\"bookId\":456," +
                "\"bookName\":\"Kafka Using Spring Boot\",\"bookAuthor\":\"Dilip\"}}"

        kafkaTemplate.sendDefault(json).get()

        //when
        var latch = CountDownLatch(1)
        latch.await(3, TimeUnit.SECONDS)

        //then
        verify(exactly = 1) { libraryEventsConsumerSpy.onMessage(any()) }
        verify(exactly = 1) { libraryEventsServiceSpy.processLibraryEvent(any()) }

        val persistedLibraryEvent = libraryEventsRepository.findById(libraryEventId)
        assertFalse(persistedLibraryEvent.isPresent)
    }

    @Test
    fun publishModifyLibraryEventNullLibraryEventId() {

        //given
        val libraryEventId = null
        val json = "{\"libraryEventId\":$libraryEventId,\"libraryEventType\":\"UPDATE\",\"book\":{\"bookId\":456," +
                "\"bookName\":\"Kafka Using Spring Boot\",\"bookAuthor\":\"Dilip\"}}"

        kafkaTemplate.sendDefault(json).get()

        //when
        var latch = CountDownLatch(1)
        latch.await(3, TimeUnit.SECONDS)

        //then
        verify(exactly = 1) { libraryEventsConsumerSpy.onMessage(any()) }
        verify(exactly = 1) { libraryEventsServiceSpy.processLibraryEvent(any()) }

    }

    @Test
    fun publishModifyLibraryEventZerolLibraryEventId() {

        //given
        val libraryEventId = 0
        val json = "{\"libraryEventId\":$libraryEventId,\"libraryEventType\":\"UPDATE\",\"book\":{\"bookId\":456," +
                "\"bookName\":\"Kafka Using Spring Boot\",\"bookAuthor\":\"Dilip\"}}"

        kafkaTemplate.sendDefault(json).get()

        //when
        var latch = CountDownLatch(1)
        latch.await(3, TimeUnit.SECONDS)

        //then
        //verify(exactly = 4) { libraryEventsConsumerSpy.onMessage(any()) }
        verify(exactly = 4) { libraryEventsServiceSpy.processLibraryEvent(any()) }
        verify(exactly = 1) { libraryEventsServiceSpy.handleRecovery(any()) }

    }
}