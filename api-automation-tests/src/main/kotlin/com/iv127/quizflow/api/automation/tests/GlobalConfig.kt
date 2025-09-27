package com.iv127.quizflow.api.automation.tests

data class GlobalConfig(val baseUrl: String = "http://localhost:8080") {
    companion object {
        val INSTANCE = GlobalConfig()
    }
}
