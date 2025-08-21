package com.iv127.quizflow.core.model.quiz.question.resolver

import com.iv127.quizflow.core.model.quiz.question.Question
import com.iv127.quizflow.core.model.quiz.question.lang.Outcome

interface QuestionsResolver {

    fun getType(): QuestionsResolverType;

    fun resolve(input: String): Outcome<List<Question>, QuestionsResolveException>;

}
