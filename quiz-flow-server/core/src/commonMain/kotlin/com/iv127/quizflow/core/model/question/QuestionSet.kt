package com.iv127.quizflow.core.model.question

import com.iv127.quizflow.core.rest.api.InstantSerializer
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalTime::class)
@Serializable
data class QuestionSet(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String,
    @SerialName("latest_version") val latestVersion: Int,
    @SerialName("created_date") @Serializable(with = InstantSerializer::class) val createdDate: Instant,
)
