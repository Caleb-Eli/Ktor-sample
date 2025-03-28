package com.example.services

import com.example.models.Todo
import com.example.models.TodoResponse
import com.example.models.User
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.Document
import org.bson.types.ObjectId

class TodoService(private val database: MongoDatabase) {
    var collection: MongoCollection<Document>


    init {
        database.createCollection("todos")
        collection = database.getCollection("todos")
    }

    // Create new todo
    suspend fun create(todo: Todo): TodoResponse = withContext(Dispatchers.IO) {
        val doc = todo.toDocument()
        collection.insertOne(doc)
        val todoDocument = Todo.fromDocument(doc)
        TodoResponse(
            todoId =  doc["_id"].toString(),
            description =  todoDocument.description,
            status =  todoDocument.status,
            name =  todoDocument.name,
            userId = todoDocument.userId
        )
    }

    // Read a todo
    suspend fun read(id: String): TodoResponse? = withContext(Dispatchers.IO) {
        val doc = collection.find(Filters.eq("_id", ObjectId(id))).first()
        if(doc!= null){
            val todoDocument = Todo.fromDocument(doc)
            TodoResponse(
                todoId =  doc["_id"].toString(),
                description =  todoDocument.description,
                status =  todoDocument.status,
                name =  todoDocument.name,
                userId = todoDocument.userId
            )
        }
        else {null}

    }

    // Update a todo
    suspend fun update(id: String, todo: Todo): TodoResponse? = withContext(Dispatchers.IO) {
      val doc =  collection.findOneAndReplace(Filters.eq("_id", ObjectId(id)), todo.toDocument())
        if(doc!= null){
            val todoDocument = Todo.fromDocument(doc)
            TodoResponse(
                todoId =  doc["_id"].toString(),
                description =  todoDocument.description,
                status =  todoDocument.status,
                name =  todoDocument.name,
                userId = todoDocument.userId
            )
        }
        else {null}
    }

    // Delete a todo
    suspend fun delete(id: String): Document? = withContext(Dispatchers.IO) {
        collection.findOneAndDelete(Filters.eq("_id", ObjectId(id)))
    }
    //recuprer tous les todo dans le user
    suspend fun findByUser(userId: String): List<TodoResponse> = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("userId",userId))
            .map {
               val todoDocument = Todo.fromDocument(it)
                TodoResponse(
                   todoId =  it["_id"].toString(),
                   description =  todoDocument.description,
                   status =  todoDocument.status,
                    name =  todoDocument.name,
                    userId = todoDocument.userId
                )

            }
            .toList()
    }
}


