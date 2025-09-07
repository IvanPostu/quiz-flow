package com.iv127.quizflow.core.sqlite

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.alloc
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.plus
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import platform.posix.usleep
import sqlite3.SQLITE_BUSY
import sqlite3.sqlite3_close
import sqlite3.sqlite3_errmsg
import sqlite3.sqlite3_exec
import sqlite3.sqlite3_free
import sqlite3.sqlite3_open

@OptIn(ExperimentalForeignApi::class)
typealias DbConnection = CPointer<cnames.structs.sqlite3>?

@OptIn(ExperimentalForeignApi::class)
private fun fromCArray(ptr: CPointer<CPointerVar<ByteVar>>, count: Int) =
    Array(count, { index -> (ptr + index)!!.pointed.value!!.toKString() })

@OptIn(ExperimentalForeignApi::class)
class KSqlite {
    var dbPath: String = ""
    var db: DbConnection = null

    constructor(dbPath: String) {
        memScoped {
            val dbPtr = alloc<CPointerVar<cnames.structs.sqlite3>>()
            if (sqlite3_open(dbPath, dbPtr.ptr) != 0) {
                throw IllegalStateException("Cannot open db: ${sqlite3_errmsg(dbPtr.value)}")
            }
            db = dbPtr.value
        }
    }

    constructor(db: COpaquePointer?) {
        this.db = db?.reinterpret()
    }

    val cpointer
        get() = db as COpaquePointer?

    fun execute(command: String, callback: ((Array<String>, Array<String>) -> Int)? = null) {
        memScoped {
            val error = this.alloc<CPointerVar<ByteVar>>()
            val callbackStable = if (callback != null) StableRef.create(callback) else null
            try {
                var attempts = 0
                var rc: Int

                do {
                    if (attempts > 0) {
                        usleep(200_000u); // 0.2 seconds
                    }
                    rc = sqlite3_exec(
                        db, command, if (callback != null)
                            staticCFunction { ptr, count, data, columns ->
                                val callbackFunction =
                                    ptr!!.asStableRef<(Array<String>, Array<String>) -> Int>().get()
                                val columnsArray = fromCArray(columns!!, count)
                                val dataArray = fromCArray(data!!, count)
                                callbackFunction(columnsArray, dataArray)
                            } else null, callbackStable?.asCPointer(), error.ptr)
                    attempts++;
                } while (rc == SQLITE_BUSY && attempts < 5)

                if (rc != 0) {
                    throw IllegalStateException("sqlite3_exec failed with code: $rc - ${error.value!!.toKString()}")
                }
            } finally {
                callbackStable?.dispose()
                sqlite3_free(error.value)
            }
        }
    }

    // TODO: use sql3_prepare instead!
    fun escape(input: String): String = input.replace("'", "''")

    override fun toString(): String = "SQLite database in $dbPath"

    fun close() {
        if (db != null) {
            sqlite3_close(db)
            db = null
        }
    }
}

inline fun withSqlite(path: String, function: (KSqlite) -> Unit) {
    val db = KSqlite(path)
    try {
        function(db)
    } finally {
        db.close()
    }
}

inline fun withSqlite(db: KSqlite, function: (KSqlite) -> Unit) {
    try {
        function(db)
    } finally {
        db.close()
    }
}
