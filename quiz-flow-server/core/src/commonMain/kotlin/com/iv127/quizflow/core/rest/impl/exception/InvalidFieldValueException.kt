package com.iv127.quizflow.core.rest.impl.exception

class InvalidFieldValueException internal constructor(
    val fieldName: String,
    val fieldValue: String?,
    val msg: String,
) : Exception("Invalid field value, field: $fieldName, value: $fieldValue, message: $msg")
