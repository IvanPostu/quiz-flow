package com.iv127.quizflow.core.rest.impl.quizresult

class FinalizedQuizNotFoundException(val quizId: String, val reason: String) :
    Exception("Finalized quiz for id $quizId was not found due to the reason: $reason")
