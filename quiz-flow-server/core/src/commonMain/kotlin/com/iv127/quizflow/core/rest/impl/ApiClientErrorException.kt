package com.iv127.quizflow.core.rest.impl

data class ApiClientErrorException(
    val errorCode: String,
    val msg: String,
    val data: Map<String, String> = mapOf()
) : Exception("Code: $errorCode, Message: $msg, Data: $data")
