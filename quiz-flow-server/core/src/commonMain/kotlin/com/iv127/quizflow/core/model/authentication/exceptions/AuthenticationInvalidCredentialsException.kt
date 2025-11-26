package com.iv127.quizflow.core.model.authentication.exceptions

import com.iv127.quizflow.core.security.AuthenticationException

class AuthenticationInvalidCredentialsException : AuthenticationException("Credentials are invalid")
