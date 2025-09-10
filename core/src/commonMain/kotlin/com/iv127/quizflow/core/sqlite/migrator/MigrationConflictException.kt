package com.iv127.quizflow.core.sqlite.migrator

class MigrationConflictException(message: String, cause: Throwable? = null) : IllegalStateException(message, cause) {
}
