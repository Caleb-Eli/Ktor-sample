package com.example.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.bson.Document

@Serializable
data class Todo(
    val name: String,
    val description: String,
    val status: String,
    val userId : String
) {
    fun toDocument(): Document = Document.parse(Json.encodeToString(this))

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromDocument(document: Document): Todo = json.decodeFromString(document.toJson())
    }
}

@Serializable
data class TodoResponse(
    val name: String,
    val description: String,
    val status: String,
    val userId : String,
    val todoId : String
)