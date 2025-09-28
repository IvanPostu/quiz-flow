package com.iv127.quizflow.core.sqlite.migrator

import com.iv127.quizflow.core.platform.file.PlatformPath
import com.iv127.quizflow.core.resource.Resource
import com.iv127.quizflow.core.sqlite.SqliteDatabase
import com.iv127.quizflow.core.sqlite.SqliteTimestampUtils
import com.iv127.quizflow.core.utils.getClassFullName
import io.ktor.util.logging.KtorSimpleLogger
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class DatabaseMigrator(
    private val platformPath: PlatformPath,
    private val resource: Resource,
    private val migrationName: String = "app"
) {

    companion object {
        const val MIGRATION_TABLE_NAME = "migration_history"
        private const val MIGRATION_RESOURCE_FOLDER = "migrations"
        private val LOG = KtorSimpleLogger(getClassFullName(DatabaseMigrator::class))

        // TODO room for improvement, it shouldn't be global
        private var MIGRATION_APPLIED = false
    }

    fun migrate(db: SqliteDatabase): SqliteDatabase {
        val databasePath = db.getDatabasePath()
        if (!checkMigrationHistoryTableExists(db)) {
            createMigrationHistoryTable(db)
        }

        if (!MIGRATION_APPLIED) {
            val sortedFilenames = getSortedMigrationFilenamesFromResource()
            val migrationRecords = selectMigrationRecords(db)

            for (i in sortedFilenames.indices) {
                val migrationFilename = sortedFilenames[i]
                if (i < migrationRecords.size) {
                    val migrationRecord = migrationRecords[i]
                    if (migrationFilename != migrationRecord.filename) {
                        throw MigrationConflictException(
                            "Migration filename: $migrationFilename is not equal to the one from migration history table: $migrationRecord.filename"
                        )
                    }
                    if (!migrationFilename.startsWith("" + migrationRecord.version) || !migrationRecord.filename.startsWith(
                            "" + migrationRecord.version
                        )
                    ) {
                        throw MigrationConflictException(
                            "Migrations history table is corrupted, migrationRecord.version=${migrationRecord.version}, migrationRecord.filename=${migrationRecord.filename}, migrationFilename=$migrationFilename"
                        )
                    }
                    val migrationContent = readMigrationScriptContent(migrationFilename)
                    if (migrationContent != migrationRecord.migrationContent) {
                        throw MigrationConflictException(
                            "Migration content for the filename: $migrationFilename is not equal to the one from migration history table"
                        )
                    }
                    continue
                }
                val migrationContent = readMigrationScriptContent(migrationFilename)
                val version = extractNumberBeforeUnderscore(migrationFilename)
                executeMigration(db, migrationContent, migrationFilename)
                insertMigrationRecord(db, migrationContent, migrationFilename, version)
                LOG.info("Migration: $migrationFilename has been successfully applied")
            }
            MIGRATION_APPLIED = true
            LOG.info("Migration was successfully applied to the database: $databasePath")
        }
        return db
    }

    private fun insertMigrationRecord(
        db: SqliteDatabase,
        migrationContent: String,
        migrationFilename: String,
        version: Int
    ) {
        val appliedAt = SqliteTimestampUtils.toValue(Clock.System.now())
        db.executeAndGetChangedRowsCount(
            """
                INSERT INTO migration_history (
                    version,
                    filename,
                    migration_content,
                    applied_at) VALUES (?, ?, ?, ?);
            """.trimIndent(),
            listOf<Any?>(version, migrationFilename, migrationContent, appliedAt)
        )
    }

    private fun executeMigration(db: SqliteDatabase, migrationContent: String, migrationFilename: String) {
        try {
            db.executeAndGetChangedRowsCount(migrationContent)
        } catch (e: Exception) {
            throw MigrationConflictException(
                "Migration script: $migrationFilename failed with the message: ${e.message}", e
            )
        }
    }

    private fun checkMigrationHistoryTableExists(db: SqliteDatabase): Boolean {
        val result =
            db.executeAndGetResultSet("SELECT name FROM sqlite_master WHERE type='table' AND name='$MIGRATION_TABLE_NAME';")
        return result.isNotEmpty()
    }

    private fun readMigrationScriptContent(filename: String): String {
        return resource.readResource(MIGRATION_RESOURCE_FOLDER, migrationName, filename).decodeToString()
    }

    private fun getSortedMigrationFilenamesFromResource(): List<String> {
        val migrationsLocation = platformPath.resolve(resource.resourcePath, MIGRATION_RESOURCE_FOLDER, migrationName)
        val versionsWithFilenamesSorted: List<Pair<Int, String>> =
            platformPath.getFilenamesFromDirectory(migrationsLocation)
                .map {
                    val version = extractNumberBeforeUnderscore(it)
                    Pair(version, it)
                }.sortedBy {
                    it.first
                }

        for (i in versionsWithFilenamesSorted.indices) {
            if (i != versionsWithFilenamesSorted[i].first) {
                throw MigrationConflictException(
                    "Migration version for the filename: ${versionsWithFilenamesSorted[i].second} should be equal to $i"
                )
            }
        }

        return versionsWithFilenamesSorted.map {
            it.second
        }
    }

    private fun extractNumberBeforeUnderscore(input: String): Int {
        val regex = """^(\d+)__""".toRegex()
        val matchResult = regex.find(input)
        return matchResult?.groups?.get(1)?.value?.toInt()!!
    }

    private fun selectMigrationRecords(db: SqliteDatabase): List<MigrationRecord> {
        val dbResult = db.executeAndGetResultSet("SELECT t.* FROM $MIGRATION_TABLE_NAME AS t ORDER BY t.version ASC;")
        return dbResult.map { resultSet ->
            val version = resultSet["version"]?.toInt()!!
            val filename = resultSet["filename"]!!
            val migrationContent = resultSet["migration_content"]!!
            val appliedAt = resultSet["applied_at"]!!
            MigrationRecord(version, filename, migrationContent, SqliteTimestampUtils.fromValue(appliedAt))
        }
    }

    private fun createMigrationHistoryTable(db: SqliteDatabase) {
        db.executeAndGetChangedRowsCount(
            """
            CREATE TABLE migration_history (
                version INT PRIMARY KEY,
                filename VARCHAR(255),
                migration_content TEXT,
                applied_at TIMESTAMP
            );
        """.trimIndent()
        )
    }

}
