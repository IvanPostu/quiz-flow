package com.iv127.quizflow.core.model.authentication.exceptions

import com.iv127.quizflow.core.security.AuthenticationException

class RefreshTokenExpiredException : AuthenticationException("Refresh token is expired")
