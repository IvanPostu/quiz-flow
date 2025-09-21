package com.iv127.quizflow.core.sqlite

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.NativePlacement
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.UByteVarOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.plus
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.set
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import platform.posix.usleep
import sqlite3.SQLITE_BUSY
import sqlite3.SQLITE_DONE
import sqlite3.SQLITE_OK
import sqlite3.SQLITE_ROW
import sqlite3.SQLITE_STATIC
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
class KSqlite : AutoCloseable {
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

    fun executeStatement(
        command: String,
        args: List<Any>,
        callback: ((Array<String?>, Array<String?>) -> Int)? = null
    ): Int {
        memScoped {
            val stmt = alloc<CPointerVar<cnames.structs.sqlite3_stmt>>()
            try {
                if (sqlite3_prepare_v2(db, command, -1, stmt.ptr, null) != SQLITE_OK) {
                    throw IllegalStateException("Cannot prepare statement: ${sqlite3_errmsg(db)}")
                }

                for (i in args.indices) {
                    val arg = args[i]
                    val str = arg as String
                    println(str)
                    if (sqlite3_bind_text(stmt.value, i + 1, str, -1, SQLITE_STATIC) != SQLITE_OK) {
                        throw IllegalStateException("Cannot bind argument: ${sqlite3_errmsg(db)}")
                    }
                }

                val numColumns: Int = sqlite3_column_count(stmt.value)
                val columnNames: Array<String?> = arrayOfNulls(numColumns)
                for (i in 0 until numColumns) {
                    val textBytes: CPointer<kotlinx.cinterop.ByteVarOf<Byte>>? = sqlite3_column_name(stmt.value, i)
                    if (textBytes != null) {
                        columnNames[i] = textBytes.toKString()
                    }
                }
                if (callback != null) {
                    callback(columnNames, emptyArray())
                }

                while (true) {
                    val rc = sqlite3_step(stmt.value);

                    if (rc == SQLITE_ROW) {  // SELECT query
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
                        return sqlite3_changes(db)
                    } else {
                        throw IllegalStateException("Execution failed: ${sqlite3_errmsg(db)?.toKString()}")
                    }
                }
            } finally {
                sqlite3_finalize(stmt.value);
            }
        }
    }

    fun execute(command: String, callback: ((Array<String?>, Array<String?>) -> Int)? = null): Int {
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
                                    ptr!!.asStableRef<(Array<String?>, Array<String?>) -> Int>().get()
                                val columnsArray = fromCArray(columns!!, count)
                                val dataArray = fromCArray(data!!, count)
                                callbackFunction(columnsArray, dataArray)
                            } else null, callbackStable?.asCPointer(), error.ptr)
                    attempts++;
                } while (rc == SQLITE_BUSY && attempts < 5)

                if (rc != 0) {
                    throw IllegalStateException("sqlite3_exec failed with code: $rc - ${error.value!!.toKString()}")
                }
                return sqlite3_changes(db)
            } finally {
                callbackStable?.dispose()
                sqlite3_free(error.value)
            }
        }
    }

    // TODO: use sql3_prepare instead!
    fun escape(input: String): String = input.replace("'", "''")

    override fun toString(): String = "SQLite database in $dbPath"

    override fun close() {
        if (db != null) {
            sqlite3_close(db)
            db = null
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

@OptIn(ExperimentalForeignApi::class)
private fun stringToCharPointer(nativePlacement: NativePlacement, str: String): CPointer<ByteVar> {
    val length = str.length
    val nativeCharPointer = nativePlacement.allocArray<ByteVar>(length + 1) // +1 for null terminator

    str.forEachIndexed { index, char ->
        nativeCharPointer[index] = char.toByte() // Convert each Char to a Byte
    }

    nativeCharPointer[length] = 0.toByte()

    return nativeCharPointer
}

private fun sstr(str: String): String {
    val zeroByte: Byte = 0
    return StringBuilder()
        .append(str)
        .append(zeroByte)
        .toString()
}
