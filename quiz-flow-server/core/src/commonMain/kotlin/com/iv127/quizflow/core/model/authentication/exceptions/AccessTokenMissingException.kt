package com.iv127.quizflow.core.model.authentication.exceptions

import com.iv127.quizflow.core.security.AuthenticationException

class AccessTokenMissingException : AuthenticationException("Access token is missing")
