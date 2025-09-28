package com.iv127.quizflow.core.sqlite.migrator

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class MigrationRecord(val version: Int, val filename: String, val migrationContent: String, val appliedAt: Instant)
