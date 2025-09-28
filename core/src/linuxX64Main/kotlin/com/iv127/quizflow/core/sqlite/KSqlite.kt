package com.iv127.quizflow.core.sqlite

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.UByteVarOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.plus
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import platform.posix.usleep
import sqlite3.SQLITE_BUSY
import sqlite3.SQLITE_DONE
import sqlite3.SQLITE_OK
import sqlite3.SQLITE_ROW
import sqlite3.SQLITE_TRANSIENT
import sqlite3.sqlite3_bind_blob
import sqlite3.sqlite3_bind_double
import sqlite3.sqlite3_bind_int
import sqlite3.sqlite3_bind_int64
import sqlite3.sqlite3_bind_null
import sqlite3.sqlite3_bind_text
import sqlite3.sqlite3_changes
import sqlite3.sqlite3_close
import sqlite3.sqlite3_column_count
import sqlite3.sqlite3_column_name
import sqlite3.sqlite3_column_text
import sqlite3.sqlite3_errmsg
import sqlite3.sqlite3_exec
import sqlite3.sqlite3_finalize
import sqlite3.sqlite3_free
import sqlite3.sqlite3_open
import sqlite3.sqlite3_prepare_v2
import sqlite3.sqlite3_step

@OptIn(ExperimentalForeignApi::class)
typealias DbConnection = CPointer<cnames.structs.sqlite3>?

@OptIn(ExperimentalForeignApi::class)
private fun fromCArray(ptr: CPointer<CPointerVar<ByteVar>>, count: Int) =
    Array(count, { index ->
        val value = (ptr + index)!!.pointed.value
        value?.toKString()
    })

@OptIn(ExperimentalForeignApi::class)
class KSqlite(dbPath: String) : AutoCloseable {
    var dbPath: String = ""
    var db: DbConnection = null

    init {
        memScoped {
            val dbPtr = alloc<CPointerVar<cnames.structs.sqlite3>>()
            if (sqlite3_open(dbPath, dbPtr.ptr) != 0) {
                throw IllegalStateException("Cannot open db: ${sqlite3_errmsg(dbPtr.value)}")
            }
            db = dbPtr.value
        }
    }

    fun executeStatement(
        command: String,
        args: List<Any?>,
        callback: ((Array<String?>, Array<String?>) -> Int)? = null
    ): Int {
        memScoped {
            val stmt = alloc<CPointerVar<cnames.structs.sqlite3_stmt>>()
            try {
                val changesCountBeforeOperation = sqlite3_changes(db)
                if (sqlite3_prepare_v2(db, command, -1, stmt.ptr, null) != SQLITE_OK) {
                    throw IllegalStateException("Cannot prepare statement: ${sqlite3_errmsg(db)}")
                }
                populateStatement(stmt, args)

                val numColumns: Int = sqlite3_column_count(stmt.value)
                val columnNames: Array<String?> = arrayOfNulls(numColumns)
                for (i in 0 until numColumns) {
                    val textBytes: CPointer<kotlinx.cinterop.ByteVarOf<Byte>>? = sqlite3_column_name(stmt.value, i)
                    if (textBytes != null) {
                        columnNames[i] = textBytes.toKString()
                    }
                }
                var attempts = 0
                var rc = 0
                while (attempts < 5) {
                    rc = sqlite3_step(stmt.value)
                    if (rc == SQLITE_ROW) {
                        val values: Array<String?> = arrayOfNulls(numColumns)
                        for (i in 0 until numColumns) {
                            val res: CPointer<UByteVarOf<UByte>>? = sqlite3_column_text(stmt.value, i)
                            if (res != null) {
                                values[i] = cPointerToString(res)
                            }
                        }
                        if (callback != null) {
                            callback(columnNames, values)
                        }
                    } else if (rc == SQLITE_DONE) {
                        break
                    } else if (rc == SQLITE_BUSY) {
                        usleep(200_000u)
                        attempts++
                    } else {
                        throw IllegalStateException("Execution failed: ${sqlite3_errmsg(db)?.toKString()}")
                    }
                }
                if (attempts == 5) {
                    throw IllegalStateException("sqlite3_step failed with code: $rc, attempts: $attempts")
                }
                return sqlite3_changes(db) - changesCountBeforeOperation
            } finally {
                sqlite3_finalize(stmt.value)
            }
        }
    }

    fun execute(command: String, callback: ((Array<String?>, Array<String?>) -> Int)? = null): Int {
        memScoped {
            val error = this.alloc<CPointerVar<ByteVar>>()
            val callbackStable = if (callback != null) StableRef.create(callback) else null
            try {
                val rc: Int = executeWithRetries(1000, 5) {
                    sqlite3_exec(
                        db, command, if (callback != null)
                            staticCFunction { ptr, count, data, columns ->
                                val callbackFunction =
                                    ptr!!.asStableRef<(Array<String?>, Array<String?>) -> Int>().get()
                                val columnsArray = fromCArray(columns!!, count)
                                val dataArray = fromCArray(data!!, count)
                                callbackFunction(columnsArray, dataArray)
                            } else null, callbackStable?.asCPointer(), error.ptr)
                }

                if (rc != 0) {
                    throw IllegalStateException(
                        "sqlite3_exec failed with code: $rc, message:${error.value?.toKString()}, statement: $command"
                    )
                }
                return sqlite3_changes(db)
            } finally {
                callbackStable?.dispose()
                sqlite3_free(error.value)
            }
        }
    }

    override fun toString(): String = "SQLite database in $dbPath"

    override fun close() {
        if (db != null) {
            sqlite3_close(db)
            db = null
        }
    }

    private fun executeWithRetries(timeRoomMillis: Long, retries: Int, closure: () -> Int): Int {
        val oneAttemptDuration = timeRoomMillis / retries
        var attempts = 0
        while (true) {
            if (attempts > 0) {
                usleep((oneAttemptDuration * 1000).toUInt()) // 0.2 seconds
            }
            val result = closure()
            if (result == SQLITE_BUSY && attempts < 5) {
                attempts++
                continue
            }
            return result
        }
    }

    private fun populateStatement(stmt: CPointerVar<cnames.structs.sqlite3_stmt>, args: List<Any?>) {
        for (i in args.indices) {
            val arg = args[i]
            if (arg is String) {
                if (sqlite3_bind_text(stmt.value, i + 1, arg, -1, SQLITE_TRANSIENT) != SQLITE_OK) {
                    throw IllegalStateException("Cannot bind argument: ${sqlite3_errmsg(db)}")
                }
                continue
            }
            if (arg is Int) {
                if (sqlite3_bind_int(stmt.value, i + 1, arg) != SQLITE_OK) {
                    throw IllegalStateException("Cannot bind argument: ${sqlite3_errmsg(db)}")
                }
                continue
            }
            if (arg is Long) {
                if (sqlite3_bind_int64(stmt.value, i + 1, arg) != SQLITE_OK) {
                    throw IllegalStateException("Cannot bind argument: ${sqlite3_errmsg(db)}")
                }
                continue
            }
            if (arg is Double) {
                if (sqlite3_bind_double(stmt.value, i + 1, arg) != SQLITE_OK) {
                    throw IllegalStateException("Cannot bind argument: ${sqlite3_errmsg(db)}")
                }
                continue
            }
            if (arg is Float) {
                if (sqlite3_bind_double(stmt.value, i + 1, arg.toDouble()) != SQLITE_OK) {
                    throw IllegalStateException("Cannot bind argument: ${sqlite3_errmsg(db)}")
                }
                continue
            }
            if (arg is ByteArray) {
                if (sqlite3_bind_blob(stmt.value, i + 1, arg.refTo(0), -1, SQLITE_TRANSIENT) != SQLITE_OK) {
                    throw IllegalStateException("Cannot bind argument: ${sqlite3_errmsg(db)}")
                }
                continue
            }
            if (arg == null) {
                if (sqlite3_bind_null(stmt.value, i + 1) != SQLITE_OK) {
                    throw IllegalStateException("Cannot bind argument: ${sqlite3_errmsg(db)}")
                }
                continue
            }
            throw IllegalArgumentException("Illegal argument[$i] - $arg")
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun cPointerToString(cPointer: CPointer<UByteVarOf<UByte>>): String {
    val byteArray = mutableListOf<Byte>()

    var currentPointer = cPointer
    while (currentPointer.pointed.value.toInt() != 0) {
        byteArray.add(currentPointer.pointed.value.toByte())
        currentPointer = (currentPointer + 1)!!
    }

    val byteArr = byteArray.toByteArray()

    return byteArr.toKString()
}
