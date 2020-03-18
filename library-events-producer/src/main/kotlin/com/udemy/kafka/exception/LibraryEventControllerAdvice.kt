package com.udemy.kafka.exception

import org.apache.logging.log4j.LogManager
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.util.stream.Collectors

@ControllerAdvice
class LibraryEventControllerAdvice {

    private val logger = LogManager.getLogger(javaClass)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleRequestBody(ex: MethodArgumentNotValidException): ResponseEntity<Any> {

        val errorList = ex.bindingResult.fieldErrors

        val errorMessage = errorList.stream()
                .map { fieldError: FieldError -> fieldError.field + " - " + fieldError.defaultMessage }
                .sorted()
                .collect(Collectors.joining(", "))

        logger.info("errorMessage : $errorMessage")

        return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
    }
}