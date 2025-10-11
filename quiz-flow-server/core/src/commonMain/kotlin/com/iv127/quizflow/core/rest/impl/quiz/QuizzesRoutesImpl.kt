package com.iv127.quizflow.core.rest.impl.quiz

import com.iv127.quizflow.core.model.authorization.Authorization
import com.iv127.quizflow.core.model.question.InvalidQuestionSetVersionException
import com.iv127.quizflow.core.model.question.Question
import com.iv127.quizflow.core.model.question.QuestionSetNotFoundException
import com.iv127.quizflow.core.model.question.QuestionSetVersion
import com.iv127.quizflow.core.model.quizz.FinalizedQuizUpdateException
import com.iv127.quizflow.core.model.quizz.InvalidQuizAnswerException
import com.iv127.quizflow.core.model.quizz.Quiz
import com.iv127.quizflow.core.model.quizz.QuizAnswer
import com.iv127.quizflow.core.model.quizz.QuizNotFoundException
import com.iv127.quizflow.core.rest.ApiRoute
import com.iv127.quizflow.core.rest.api.authorization.ApiAuthorization
import com.iv127.quizflow.core.rest.api.quiz.QuizAnswerResponse
import com.iv127.quizflow.core.rest.api.quiz.QuizCreateRequest
import com.iv127.quizflow.core.rest.api.quiz.QuizQuestionResponse
import com.iv127.quizflow.core.rest.api.quiz.QuizResponse
import com.iv127.quizflow.core.rest.api.quiz.QuizUpdateRequest
import com.iv127.quizflow.core.rest.api.quiz.QuizzesRoutes
import com.iv127.quizflow.core.rest.api.quiz.QuizzesRoutes.Companion.ROUTE_PATH
import com.iv127.quizflow.core.rest.impl.exception.ApiClientErrorExceptionTranslator
import com.iv127.quizflow.core.security.AuthenticationProvider
import com.iv127.quizflow.core.server.JsonWebResponse
import com.iv127.quizflow.core.server.routingContextWebResponse
import com.iv127.quizflow.core.services.questionset.QuestionSetService
import com.iv127.quizflow.core.services.quiz.QuizService
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import org.koin.core.KoinApplication

@OptIn(ExperimentalTime::class)
class QuizzesRoutesImpl(koinApp: KoinApplication) : QuizzesRoutes, ApiRoute {

    private val questionSetService: QuestionSetService by koinApp.koin.inject()
    private val quizService: QuizService by koinApp.koin.inject()

    override fun setup(parent: Route) {
        parent.get("$ROUTE_PATH/{quizId}", routingContextWebResponse {
            val quizId = call.parameters["quizId"] ?: ""
            val authorization = AuthenticationProvider.provide(call)
            JsonWebResponse.create(get(authorization, quizId))
        })
        parent.post(ROUTE_PATH, routingContextWebResponse {
            val request = call.receive<QuizCreateRequest>()
            val authorization = AuthenticationProvider.provide(call)
            JsonWebResponse.create(create(authorization, request))
        })
        parent.put("$ROUTE_PATH/{quizId}", routingContextWebResponse {
            val quizId = call.parameters["quizId"] ?: ""
            val request = call.receive<QuizUpdateRequest>()
            val authorization = AuthenticationProvider.provide(call)
            JsonWebResponse.create(update(authorization, quizId, request))
        })
    }

    override suspend fun get(authorization: ApiAuthorization, quizId: String): QuizResponse {
        try {
            val quiz = quizService.getQuiz(authorization as Authorization, quizId)
            val questionSetVersion = getQuestionSetVersion(quiz.questionSetId, quiz.questionSetVersion)
            val questionsById: Map<String, Question> = questionSetVersion.questions.associateBy { item -> item.id }
            return mapToQuizResponse(quiz, questionsById)
        } catch (e: QuizNotFoundException) {
            throw ApiClientErrorExceptionTranslator.translateAndThrowOrElseFail(
                e,
                QuizNotFoundException::class,
            )
        }

    }

    override suspend fun create(authorization: ApiAuthorization, request: QuizCreateRequest): QuizResponse {
        val questionSetVersion = getQuestionSetVersion(request.questionSetId, request.questionSetVersion)
        val questionsById: Map<String, Question> = questionSetVersion.questions.associateBy { item -> item.id }
        val questions: MutableList<Question> = mutableListOf()
        for (requestQuestionId in request.questionIds) {
            questions.add(questionsById[requestQuestionId]!!)
        }

        val createdQuiz = quizService.createQuiz(authorization as Authorization, questionSetVersion, questions)
        return mapToQuizResponse(createdQuiz, questionsById)
    }

    override suspend fun update(
        authorization: ApiAuthorization,
        quizId: String,
        request: QuizUpdateRequest
    ): QuizResponse {

        try {
            val quiz = quizService.getQuiz(authorization as Authorization, quizId)
            val questionSetVersion = getQuestionSetVersion(quiz.questionSetId, quiz.questionSetVersion)
            val questionsById: Map<String, Question> = questionSetVersion.questions.associateBy { item -> item.id }

            val updatedQuiz = quizService
                .updateQuiz(authorization, quizId, questionSetVersion) { quizBuilder ->
                    quizBuilder.withAnswers(
                        request.quizAnswerRequests.map {
                            QuizAnswer(it.questionId, it.chosenAnswerIndexes)
                        }
                    )
                    if (request.finalize) {
                        quizBuilder.withFinalized()
                    }
                }
            return mapToQuizResponse(updatedQuiz, questionsById)
        } catch (e: Exception) {
            throw ApiClientErrorExceptionTranslator.translateAndThrowOrElseFail(
                e,
                QuizNotFoundException::class,
                FinalizedQuizUpdateException::class,
                InvalidQuizAnswerException::class
            )
        }

    }

    private fun getQuestionSetVersion(questionSetId: String, version: Int?): QuestionSetVersion {
        try {
            val (_, questionSetVersion) = questionSetService.getQuestionSetWithVersionOrElseLatest(
                questionSetId,
                version
            )
            return questionSetVersion
        } catch (e: Exception) {
            throw ApiClientErrorExceptionTranslator.translateAndThrowOrElseFail(
                e,
                QuestionSetNotFoundException::class,
                InvalidQuestionSetVersionException::class
            )
        }
    }

    private fun mapToQuizResponse(quiz: Quiz, questionsById: Map<String, Question>): QuizResponse {
        val isFinalized = quiz.finalizedDate != Instant.DISTANT_PAST
        return QuizResponse(
            quiz.id,
            quiz.questionSetId,
            quiz.questionSetVersion,
            quiz.createdDate,
            if (isFinalized) quiz.finalizedDate else null,
            isFinalized,
            quiz.quizQuestions.map { mapQuestionToResponse(questionsById[it.questionId]!!) },
            quiz.quizAnswers.map { mapAnswerToResponse(it) }
        )
    }

    private fun mapQuestionToResponse(question: Question): QuizQuestionResponse {
        return QuizQuestionResponse(question.id, question.question, question.answerOptions)
    }

    private fun mapAnswerToResponse(quizAnswer: QuizAnswer): QuizAnswerResponse {
        return QuizAnswerResponse(quizAnswer.questionId, quizAnswer.chosenAnswerIndexes)
    }

}
