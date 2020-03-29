package com.udemy.kafka.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.udemy.kafka.entity.LibraryEvent
import com.udemy.kafka.entity.LibraryEventType
import com.udemy.kafka.repository.LibraryEventsRepository
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.RecoverableDataAccessException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import org.springframework.util.concurrent.ListenableFutureCallback

@Service
class LibraryEventsService {

    private val logger = LogManager.getLogger(javaClass)

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var libraryEventsRepository: LibraryEventsRepository

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<Int, String>

    fun processLibraryEvent(consumerRecord: ConsumerRecord<Int, String>) {

        val libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent::class.java)
        logger.info("LibraryEvent: $libraryEvent")

        libraryEvent.libraryEventId?.let {
            it.takeIf { it != 0 } ?: throw RecoverableDataAccessException("Temporary Network Issue")
        }

        when (libraryEvent.libraryEventType) {
            LibraryEventType.NEW -> save(libraryEvent)
            LibraryEventType.UPDATE -> libraryEvent.apply { validate(this) }.also { save(it) }
            else -> logger.info("Invalid Library Event Type")

        }

    }

    private fun save(libraryEvent: LibraryEvent) {

        libraryEvent.book?.libraryEvent = libraryEvent
        libraryEventsRepository.save(libraryEvent)
        logger.info("Successfully Persisted the libary Event: $libraryEvent")
    }

    private fun validate(libraryEvent: LibraryEvent) {

        val id = libraryEvent.libraryEventId ?: throw IllegalArgumentException("Library Event Id is missing")
        libraryEventsRepository.findByIdOrNull(id) ?: throw IllegalArgumentException("Not a valid library Event")
        logger.info("Validation is successful for the library Event : $libraryEvent");
    }

    fun handleRecovery(consumerRecord: ConsumerRecord<Int, String>) {

        val key = consumerRecord.key()
        val value = consumerRecord.value()

        val listenableFuture = kafkaTemplate.sendDefault(key, value)

        listenableFuture.addCallback(object : ListenableFutureCallback<SendResult<Int, String>> {

            override fun onFailure(ex: Throwable) {
                handleFailure(ex)
            }

            override fun onSuccess(result: SendResult<Int, String>?) {
                handleSuccess(key, value, result)
            }
        })
    }

    private fun handleFailure(ex: Throwable) {

        logger.error("Error Sending the Message and the exception is ${ex.message}")

        try {
            throw ex
        } catch (throwable: Throwable) {
            logger.error("Error in OnFailure: ${throwable.message}")
        }

    }

    private fun handleSuccess(key: Int?, value: String?, result: SendResult<Int, String>?) {

        logger.info("Message Sent SuccessFully for the key : $key and the value is $value , " +
                "partition is ${result?.recordMetadata?.partition()}")
    }
}