package  com.iv127.quizflow.core.sqlite.migrator

import com.iv127.quizflow.core.platform.file.FileIO
import com.iv127.quizflow.core.platform.file.PlatformPath
import com.iv127.quizflow.core.platform.proc.PlatformProcess
import com.iv127.quizflow.core.resource.Resource
import com.iv127.quizflow.core.sqlite.LinuxSqliteDatabase
import com.iv127.quizflow.core.sqlite.migrator.DatabaseMigrator.Companion.MIGRATION_TABLE_NAME
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class DatabaseMigratorTest {

    private lateinit var pathToFile: String

    @BeforeTest
    fun setup() {
        pathToFile = PlatformProcess().runShellScriptAndGetOutput(
            "" +
                "temp_file_path=$(mktemp --suffix='.db');" +
                "echo -n \$temp_file_path;"
        ).output
    }

    @Test
    fun testAppMigrator() {
        LinuxSqliteDatabase(pathToFile).use { sqliteDatabase ->
            val migrator = DatabaseMigrator(PlatformPath(), Resource(FileIO(), PlatformProcess()))
            migrator.migrate(sqliteDatabase)
            val dbResult = sqliteDatabase
                .executeAndGetResultSet("SELECT t.* FROM $MIGRATION_TABLE_NAME AS t ORDER BY t.version ASC;")
            assertTrue(dbResult.size >= 2)
        }

    }
}
