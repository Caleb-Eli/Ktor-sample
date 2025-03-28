package com.example.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.models.User
import com.example.services.UserService
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import  java.time.Instant
import  java.time.Duration
import  java.util.Date

@Serializable
data class  UserResponse(
    val token: String,
    val user : UserResponseData,
)
@Serializable
data class UserLogin(
    val email : String,
    val password : String,
)
@Serializable
data class  UserResponseData(
    val email : String,
    val name : String,
    val userId : String

)
fun Route.authRoutes(userService: UserService) {
    route("/auth") {
        post("/register") {
         val user = call.receive<User>()
            val userExists = userService.findByEmail(user.email)
            if (userExists != null) {
              call.respond(HttpStatusCode.Conflict, "l'utilisateur existe")
            }
            else{
                val userId = userService.create(user)
                val userResponse = userService.read(userId)
                if (userResponse != null) {
                    val token = generateToken(userResponse.userId,call.application.environment.config)
                    call.respond(HttpStatusCode.Created,UserResponse(token,userResponse))

                }
                else {
                    call.respond(HttpStatusCode.InternalServerError,"une erreur c'est produit lors de la creation de l'utilisateur")
                }
            }
        }
        post("/login") {
            val loginRequest = call.receive<UserLogin>()
            val user = userService.verifyPassword(loginRequest.password, loginRequest.email)
            if (user == null) {
              call.respond(HttpStatusCode.Unauthorized, "email ou mot de passe invalid")
                return@post
            }
            val token = generateToken(user.userId,call.application.environment.config )
            call.respond(HttpStatusCode.Created,UserResponse(token,user))
        }
    }
}
private fun generateToken(userId: String, config: ApplicationConfig): String {
    val jwtSecret = config.property("jwt.secret").getString()
    val jwtAudience = config.property("jwt.audience").getString()
    val jwtIssuer = config.property("jwt.issuer").getString()
    val expirationDuration = Duration.ofDays(365)
    val expirationDate = Date.from(Instant.now().plus(expirationDuration))
    return JWT.create()
        .withAudience(jwtAudience)
        .withIssuer(jwtIssuer)
        .withClaim("userId", userId)
        .withExpiresAt(expirationDate)
        .withSubject("Authentication")
        .sign(Algorithm.HMAC256(jwtSecret))
}