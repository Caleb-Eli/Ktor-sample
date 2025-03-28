package com.example.routes

import com.example.models.Todo
import com.example.services.TodoService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.todoRoutes(todoService: TodoService){
    authenticate ("auth-jwt"){
        // Create todo
        post("/todos") {
            val todo = call.receive<Todo>()
            val id = todoService.create(todo)
            call.respond(HttpStatusCode.Created, id)
        }
        // Read todo
        get("/todos/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("No ID found")
            todoService.read(id)?.let { todo ->
                call.respond(todo)
            } ?: call.respond(HttpStatusCode.NotFound)
        }
        // Update todo
        put("/todos/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("No ID found")
            val todo = call.receive<Todo>()
            todoService.update(id, todo)?.let {
                val todoUpdate = todoService.read(it.todoId)
                call.respond(HttpStatusCode.OK,todoUpdate ?: it                                     )
            } ?: call.respond(HttpStatusCode.NotFound)

        }
        // Delete todo
        delete("/todos/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("No ID found")
            todoService.delete(id)?.let {
                call.respond(HttpStatusCode.OK)
            } ?: call.respond(HttpStatusCode.NotFound)
        }

        get("user/todos"){
            val principal = call.principal<JWTPrincipal>()!!
            val userId = principal.payload.getClaim("userId").asString()
            if (userId != null){
                val todos =
                todoService.findByUser(userId)
                call.respond(todos)
            }
            else{
                call.respond(HttpStatusCode.InternalServerError, "une erreur c'est produit lors de la recuperation de todos")
            }
        }
    }
}