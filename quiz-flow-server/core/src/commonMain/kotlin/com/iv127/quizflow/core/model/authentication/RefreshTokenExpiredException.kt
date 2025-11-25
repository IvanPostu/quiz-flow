package com.iv127.quizflow.core.model.authentication

import com.iv127.quizflow.core.security.AuthenticationException

class RefreshTokenExpiredException : AuthenticationException("Refreshable token is expired")
