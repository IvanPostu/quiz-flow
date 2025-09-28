package com.iv127.quizflow.core.model.question.resolver

import com.iv127.quizflow.core.model.question.Question
import com.iv127.quizflow.core.lang.Outcome

interface QuestionsResolver {

    fun getType(): QuestionsResolverType;

    fun resolve(input: String): Outcome<List<Question>, QuestionsResolveException>;

}
