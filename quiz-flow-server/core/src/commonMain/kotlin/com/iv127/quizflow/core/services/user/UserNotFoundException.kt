package com.iv127.quizflow.core.services.user

class UserNotFoundException(username: String) : Exception("User with the username: $username entered wong password") {
}
