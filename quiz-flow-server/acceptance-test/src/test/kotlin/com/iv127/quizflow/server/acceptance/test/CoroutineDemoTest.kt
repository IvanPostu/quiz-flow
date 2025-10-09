package com.iv127.quizflow.server.acceptance.test

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class CoroutineDemoTest {

    companion object {
        private val DATABASE_MOCK: Database = object : Database {
            private var value: String? = null

            override fun populate() {
                value = "test1"
            }

            override fun read(): String {
                return value!!
            }
        }
    }

    @Test
    fun `test fetch delayed`() = runTest {
        val beforeMillis = Clock.System.now().toEpochMilliseconds()
        val data = fetchDelayed("test1")
        val timeTaken = Clock.System.now().toEpochMilliseconds() - beforeMillis
        assertEquals("test1", data)
        assertTrue(timeTaken <= 100)
    }

    @Test
    fun testRepo1() = runTest {
        val repo = Repo1()

        launch { repo.register("Jim") }
        launch { repo.register("Nick") }

        advanceUntilIdle()
        // or runCurrent()
        // or advanceTimeBy()

        assertEquals(
            listOf("Jim", "Nick"),
            repo.getAll()
        )
    }

    @Test
    fun testRepo2() = runTest {
        val repo = Repo2Bad(DATABASE_MOCK)
        repo.initialize()

        try {
            repo.fetchData()
            fail("Expected to throw NPE")
        } catch (_: NullPointerException) {
        }
    }


    @Test
    fun testRepo3() = runTest {
        val repo = Repo3Good(
            database = DATABASE_MOCK,
            ioDispatcher = StandardTestDispatcher(testScheduler)
        )

        val beforeMillis = Clock.System.now().toEpochMilliseconds()
        repo.initialize()
        advanceUntilIdle()
        val timeTaken = Clock.System.now().toEpochMilliseconds() - beforeMillis

        assertEquals("test1", repo.fetchData())
        assertTrue(timeTaken >= 500)
    }

    @Test
    fun testRepo4() = runTest {
        val repo = Repo4Good(
            database = DATABASE_MOCK,
            ioDispatcher = StandardTestDispatcher(testScheduler)
        )

        val beforeMillis = Clock.System.now().toEpochMilliseconds()
        repo.initialize().await()
        val timeTaken = Clock.System.now().toEpochMilliseconds() - beforeMillis

        assertEquals("test1", repo.fetchData())
        assertTrue(timeTaken >= 500)
    }

    @Test
    fun testRepo5() = runTest {
        val repo = Repo5Good(
            database = DATABASE_MOCK,
            ioDispatcher = StandardTestDispatcher(testScheduler)
        )

        val beforeMillis = Clock.System.now().toEpochMilliseconds()
        repo.initialize()
        val timeTaken = Clock.System.now().toEpochMilliseconds() - beforeMillis

        assertEquals("test1", repo.fetchData())
        assertTrue(timeTaken >= 500)
    }

    private suspend fun fetchDelayed(data: String): String {
        delay(1000L)
        return data
    }
}

private class Repo1 {
    private val users: MutableList<String> = ArrayList()

    fun register(user: String) {
        users.add(user)
    }

    fun getAll(): List<String> {
        return ArrayList(users)
    }
}

private class Repo2Bad(private val database: Database) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun initialize() {
        scope.launch {
            Thread.sleep(500)
            database.populate()
        }
    }

    suspend fun fetchData(): String = withContext(Dispatchers.IO) {
        database.read()
    }
}

private class Repo3Good(
    private val database: Database,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val scope = CoroutineScope(ioDispatcher)

    fun initialize() {
        scope.launch {
            Thread.sleep(500)
            database.populate()
        }
    }

    suspend fun fetchData(): String = withContext(ioDispatcher) {
        database.read()
    }
}

private class Repo4Good(
    private val database: Database,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val scope = CoroutineScope(ioDispatcher)

    fun initialize(): Deferred<Unit> {
        return scope.async {
            Thread.sleep(500)
            database.populate()
        }
    }

    suspend fun fetchData(): String = withContext(ioDispatcher) {
        database.read()
    }
}

private class Repo5Good(
    private val database: Database,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun initialize() = withContext(ioDispatcher) {
        Thread.sleep(500)
        database.populate()
    }

    suspend fun fetchData(): String = withContext(ioDispatcher) {
        database.read()
    }
}

private interface Database {
    fun populate()
    fun read(): String
}
