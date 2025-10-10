package com.iv127.quizflow.core.rest.impl.exception

class ApiClientErrorException internal constructor(
    val errorCode: String,
    val msg: String,
    val data: Map<String, String?> = mapOf(),
    val e: Exception? = null,
) : Exception("Code: $errorCode, Message: $msg, Data: $data", e)
