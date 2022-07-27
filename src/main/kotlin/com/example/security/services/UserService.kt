package com.example.security.services

import com.example.security.models.User
import com.example.security.repositories.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) {
    fun save(user: User): User {
        return this.userRepository.save(user)
    }

    fun findByEmail(email: String): User?{
        return userRepository.findByEmail(email)
    }

    fun findById(id: Int): User?{
        return userRepository.findById(id).get()
    }
}