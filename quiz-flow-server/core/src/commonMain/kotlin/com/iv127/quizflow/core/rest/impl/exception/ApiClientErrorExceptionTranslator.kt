package com.iv127.quizflow.core.rest.impl.exception

import com.iv127.quizflow.core.model.question.InvalidQuestionSetVersionException
import com.iv127.quizflow.core.model.question.QuestionSetNotFoundException
import com.iv127.quizflow.core.services.user.UsernameAlreadyTakenException
import kotlin.reflect.KClass

object ApiClientErrorExceptionTranslator {

    private val mapper = ExceptionMapper()

    init {
        mapper.register(QuestionSetNotFoundException::class) { e ->
            ApiClientErrorException(
                "question_set_not_found",
                "Question set not found",
                mapOf("questionSetId" to e.questionSetId),
                e
            )
        }
        mapper.register(InvalidQuestionSetVersionException::class) { e ->
            ApiClientErrorException(
                "invalid_question_set_version",
                "Invalid question set version",
                mapOf(
                    "questionSetId" to e.questionSetId,
                    "version" to e.version.toString()
                ),
                e
            )
        }
        mapper.register(UsernameAlreadyTakenException::class) { e ->
            ApiClientErrorException(
                "username_was_already_taken",
                "An user with such username already exists",
                mapOf(),
                e
            )
        }
        mapper.register(InvalidFieldValueException::class) { e ->
            ApiClientErrorException(
                "invalid_field_value",
                "Field value is invalid",
                mapOf(
                    "fieldName" to e.fieldName,
                    "fieldValue" to e.fieldValue,
                    "message" to e.msg,
                ),
                e
            )
        }
    }

    @Throws(ApiClientErrorException::class, RuntimeException::class)
    fun translateAndThrowOrElseFail(e: Exception, vararg typesToHandle: KClass<*>): ApiClientErrorException {
        throw mapper.mapException(e, typesToHandle.toSet())
    }
}

private class ExceptionMapper {
    private val mappers = mutableMapOf<KClass<out Exception>, (Exception) -> ApiClientErrorException>()

    @Suppress("UNCHECKED_CAST")
    fun <E : Exception> register(clazz: KClass<E>, handler: (E) -> ApiClientErrorException) {
        mappers[clazz] = handler as (Exception) -> ApiClientErrorException
    }

    fun mapException(e: Exception, typesToHandle: Set<KClass<*>>): ApiClientErrorException {
        val mapFunction: (Exception) -> ApiClientErrorException = mappers[e::class]
            ?: throw RuntimeException("Unable to find a handler for exception type ${e::class}", e)
        if (!typesToHandle.contains(e::class)) {
            throw RuntimeException("Caught exception type ${e::class} is not present in 'typesToHandle'", e)
        }
        return mapFunction.invoke(e)
    }
}
