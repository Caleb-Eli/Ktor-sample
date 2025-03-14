package com.example

import com.mongodb.client.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureMongoDatabases() {
    val mongoDatabase = connectToMongoDB()
    val todoService = TodoService(mongoDatabase)
    routing {
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
                call.respond(HttpStatusCode.OK)
            } ?: call.respond(HttpStatusCode.NotFound)
        }
        // Delete todo
        delete("/todos/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("No ID found")
            todoService.delete(id)?.let {
                call.respond(HttpStatusCode.OK)
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    }
}
/**
 * Establishes connection with a MongoDB database.
 *
 * The following configuration properties (in application.yaml/application.conf) can be specified:
 * * `db.mongo.user` username for your database
 * * `db.mongo.password` password for the user
 * * `db.mongo.host` host that will be used for the database connection
 * * `db.mongo.port` port that will be used for the database connection
 * * `db.mongo.maxPoolSize` maximum number of connections to a MongoDB server
 * * `db.mongo.database.name` name of the database
 *
 * IMPORTANT NOTE: in order to make MongoDB connection working, you have to start a MongoDB server first.
 * See the instructions here: https://www.mongodb.com/docs/manual/administration/install-community/
 * all the paramaters above
 *
 * @returns [MongoDatabase] instance
 * */
fun Application.connectToMongoDB(): MongoDatabase {
    //val user = environment.config.tryGetString("db.mongo.user")
    //val password = environment.config.tryGetString("db.mongo.password")
    //val host = environment.config.tryGetString("db.mongo.host") ?: "127.0.0.1"
    //val port = environment.config.tryGetString("db.mongo.port") ?: "27017"
   // val maxPoolSize = environment.config.tryGetString("db.mongo.maxPoolSize")?.toInt() ?: 20
        val databaseName = "myDatabase"

    //val credentials = user?.let { userVal -> password?.let { passwordVal -> "$userVal:$passwordVal@" } }.orEmpty()
    val uri = "mongodb://localhost:27017/"
    val mongoClient = MongoClients.create(uri)
    val database = mongoClient.getDatabase(databaseName)

    monitor.subscribe(ApplicationStopped) {
        mongoClient.close()
    }

    return database
}
