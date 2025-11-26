package com.iv127.quizflow.core.security

open class AuthenticationException protected constructor(val publicMessage: String) : Exception(publicMessage)
