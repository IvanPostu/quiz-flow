package com.iv127.quizflow.core.model.question

class QuestionSetNotFoundException(val questionSetId: String) :
    Exception("Can't find question set by id: $questionSetId")
