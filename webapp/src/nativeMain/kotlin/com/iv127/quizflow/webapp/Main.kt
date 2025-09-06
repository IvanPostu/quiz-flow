package com.iv127.quizflow.webapp

import kotlin.experimental.ExperimentalNativeApi
import kotlinx.cinterop.*
import platform.posix.*

fun main() {
    println("Hello from Kotlin Native!")
    hello_world()
}

@OptIn(ExperimentalNativeApi::class)
@CName("hello_world")
external fun hello_world()


