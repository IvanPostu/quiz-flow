package com.iv127.quizflow.core.model.quiz.question.resolver

class QuestionsResolverFactory {

    fun create(type: QuestionsResolverType): QuestionsResolver {
        return when (type) {
            QuestionsResolverType.QUESTION_WRAPPED_IN_MARKDOWN_CODE_SECTION
                -> ResolverForQuestionsWrappedInMarkdownCodeSection()
        }
    }

}
