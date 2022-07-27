package com.example.security.controllers

import com.example.security.dtos.LoginDTO
import com.example.security.dtos.RegisterDTO
import com.example.security.models.User
import com.example.security.services.UserService
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse


@RestController
@RequestMapping("api")
class AuthController(private val userService: UserService) {
    @PostMapping("register")
    fun register(@RequestBody requestUser: RegisterDTO): ResponseEntity<User> {
        val user = User()
        user.name = requestUser.name
        user.email = requestUser.email
        user.password = requestUser.password
        return ResponseEntity.ok(this.userService.save(user))
    }

    @PostMapping("login")
    fun login(@RequestBody requestUser: LoginDTO, response: HttpServletResponse): ResponseEntity<Any> {
        val user =
            userService.findByEmail(requestUser.email) ?: return ResponseEntity.badRequest().body("User not found!")
        if (!user.comparePassword(requestUser.password)) {
            return ResponseEntity.badRequest().body("Invalid password!")
        }

        val issuer = user.id.toString()
        val jwt = Jwts.builder()
            .setIssuer(issuer)
            .setExpiration(Date(System.currentTimeMillis() + 60 * 60 * 24 * 1000)) //1 day
            .signWith(SignatureAlgorithm.HS512, "secret")
            .compact()

        val cookie = Cookie("jwt", jwt)
        cookie.isHttpOnly = true
        response.addCookie(cookie)
        return ResponseEntity.ok("success")
    }

    @GetMapping("user")
    fun user(@CookieValue("jwt") jwt: String?): ResponseEntity<Any> {
        try {
            if (jwt == null) {
                return ResponseEntity.status(401).body("unauthenticated")
            }
            val body = Jwts.parser().setSigningKey("secret").parseClaimsJws(jwt).body
            return return ResponseEntity.ok(userService.findById(body.issuer.toInt()))
        } catch (e: Exception) {
            return ResponseEntity.status(401).body("unauthenticated")
        }
    }

    @PostMapping("logout")
    fun logout(response: HttpServletResponse): ResponseEntity<Any> {
        var cookie = Cookie("jwt", "")
        cookie.maxAge = 0
        response.addCookie(cookie)
        return ResponseEntity.ok("success")
    }
}