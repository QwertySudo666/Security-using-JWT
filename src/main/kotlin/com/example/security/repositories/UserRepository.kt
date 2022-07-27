package com.example.security.repositories

import com.example.security.models.User
import org.springframework.data.repository.CrudRepository

interface UserRepository : CrudRepository<User, Int> {
    fun findByEmail(email: String): User?
}