package com.iv127.quizflow.core.rest.api

enum class SortOrder {
    ASC,
    DESC,
}

fun String?.toSortOrderEnumOrNull(): SortOrder? {
    return try {
        this?.let { SortOrder.valueOf(it.uppercase()) }
    } catch (e: IllegalArgumentException) {
        null
    }
}
