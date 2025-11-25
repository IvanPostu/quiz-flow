package com.iv127.quizflow.core.model.authentication

import com.iv127.quizflow.core.security.AuthenticationException

class AuthenticationAccessTokenIsInvalidException : AuthenticationException("Access token is invalid")
