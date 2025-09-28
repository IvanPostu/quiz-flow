package com.iv127.quizflow.core.services.user

class UserInvalidPasswordException(username: String) : Exception("User with the username: $username was not found") {
}
