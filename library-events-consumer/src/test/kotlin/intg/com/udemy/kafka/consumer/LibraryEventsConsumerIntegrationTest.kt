package com.udemy.kafka.consumer

import com.ninjasquad.springmockk.SpykBean
import com.udemy.kafka.entity.LibraryEvent
import com.udemy.kafka.repository.LibraryEventsRepository
import com.udemy.kafka.service.LibraryEventsService
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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


    @BeforeEach
    fun setUp() {

        for (messageListenerContainer in endpointRegistry.listenerContainers) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafkaBroker.partitionsPerTopic)
        }

    }

    @AfterEach
    fun tearDown() {
        embeddedKafkaBroker.kafkaServers.forEach { b -> b.shutdown() }
        embeddedKafkaBroker.kafkaServers.forEach { b -> b.awaitShutdown() }
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
            assertEquals(456,  libraryEvent?.book?.bookId)
        }
    }


}