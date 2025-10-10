package com.iv127.quizflow.core.model.question

class InvalidQuestionSetVersionException(
    val questionSetId: String,
    val version: Int,
    message: String = "Question set version: $version is invalid for questionSetId: $questionSetId"
) : Exception(message)
