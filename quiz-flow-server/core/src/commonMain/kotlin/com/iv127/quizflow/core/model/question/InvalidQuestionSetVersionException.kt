package com.iv127.quizflow.core.model.question

class InvalidQuestionSetVersionException(
    questionSetId: String,
    version:
    Int, message: String = "Question set version: $version is invalid for questionSetId: $questionSetId"
) :
    Exception(message)
