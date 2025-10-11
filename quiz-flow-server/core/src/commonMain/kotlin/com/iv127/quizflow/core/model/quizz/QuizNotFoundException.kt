package com.iv127.quizflow.core.model.quizz

class QuizNotFoundException(val quizId: String) : Exception("Can't find quiz by id: $quizId")
