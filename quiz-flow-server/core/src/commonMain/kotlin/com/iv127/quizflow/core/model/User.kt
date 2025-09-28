package com.iv127.quizflow.core.model

import com.iv127.quizflow.core.rest.api.InstantSerializer
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@OptIn(ExperimentalTime::class)
@Serializable
data class User(
    @SerialName("id") val id: String,
    @SerialName("username") val username: String,
    @SerialName("password_hash") val passwordHash: String,
    @SerialName("created_date") @Serializable(with = InstantSerializer::class) val createdDate: Instant,
    @SerialName("archived_date") @Serializable(with = InstantSerializer::class) val archivedDate: Instant?,
)
