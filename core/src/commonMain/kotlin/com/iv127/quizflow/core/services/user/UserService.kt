package com.iv127.quizflow.core.services.user

import com.iv127.quizflow.core.model.User

interface UserService {

    @Throws(UserNotFoundException::class, UserInvalidPasswordException::class)
    fun getByUsernameAndPassword(username: String, password: String): User

    fun create(username: String, password: String): User

    fun getAll(): List<User>

}
