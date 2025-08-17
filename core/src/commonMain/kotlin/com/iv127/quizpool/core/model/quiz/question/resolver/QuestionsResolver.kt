package com.iv127.quizpool.core.model.quiz.question.resolver

import com.iv127.quizpool.core.model.quiz.question.Question

interface QuestionsResolver {

    fun getType(): QuestionsResolverType;

    fun resolve(input: String): List<Question>;

}
