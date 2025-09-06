package com.iv127.quizflow.core.utils

import kotlin.reflect.KClass

fun getClassFullName(clazz: KClass<*>): String {
    return clazz.qualifiedName ?: clazz.toString()
}
