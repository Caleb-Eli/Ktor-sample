package com.example

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.bson.Document
import org.bson.types.ObjectId

@Serializable
data class Todo(
    val name: String,
    val description: String,
    val status: String
) {
    fun toDocument(): Document = Document.parse(Json.encodeToString(this))

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromDocument(document: Document): Todo = json.decodeFromString(document.toJson())
    }
}

class TodoService(private val database: MongoDatabase) {
    var collection: MongoCollection<Document>

    init {
        database.createCollection("todos")
        collection = database.getCollection("todos")
    }

    // Create new todo
    suspend fun create(todo: Todo): String = withContext(Dispatchers.IO) {
        val doc = todo.toDocument()
        collection.insertOne(doc)
        doc["_id"].toString()
    }

    // Read a todo
    suspend fun read(id: String): Todo? = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("_id", ObjectId(id))).first()?.let(Todo::fromDocument)
    }

    // Update a todo
    suspend fun update(id: String, todo: Todo): Document? = withContext(Dispatchers.IO) {
        collection.findOneAndReplace(Filters.eq("_id", ObjectId(id)), todo.toDocument())
    }

    // Delete a todo
    suspend fun delete(id: String): Document? = withContext(Dispatchers.IO) {
        collection.findOneAndDelete(Filters.eq("_id", ObjectId(id)))
    }
}

