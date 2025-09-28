package com.iv127.quizflow.core.platform.proc

// TODO both stderr and stdout are being kept in output, find a way to separate them for native impl.
data class ProcessExecutionResult(val returnCode: Int, val output: String)
