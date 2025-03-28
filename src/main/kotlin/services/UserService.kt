package com.example.services

import com.example.models.User
import com.example.routes.UserResponse
import com.example.routes.UserResponseData
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.Document
import org.bson.types.ObjectId
import org.mindrot.jbcrypt.BCrypt

class UserService(private val database: MongoDatabase) {
    var collection: MongoCollection<Document>

    init {
        database.createCollection("users")
        collection = database.getCollection("users")
    }

    // Create new user
    suspend fun create(user: User): String = withContext(Dispatchers.IO) {
        val newsUser = User(
            name = user.name,
            email = user.email,
            password = hashPassword(user.password),
        )
        val doc = newsUser.userDocument()
        collection.insertOne(doc)
        doc["_id"].toString()
    }

    // Read a user
    fun read(userId: String): UserResponseData? {
        val user = collection.find(Filters.eq("userId",userId)).first()
        if (user != null){
            val userDocument = user.let(User::fromDocument)
           return UserResponseData(userId = user["_id"].toString(),name = userDocument.name, email= userDocument.email)
        }
       return null
    }

    // Update a user
    suspend fun update(id: String, user: User): Document? = withContext(Dispatchers.IO) {
        collection.findOneAndReplace(Filters.eq("_id", ObjectId(id)), user.userDocument())
    }

    // Delete a user
    suspend fun delete(id: String): Document? = withContext(Dispatchers.IO) {
        collection.findOneAndDelete(Filters.eq("_id", ObjectId(id)))
    }
    fun findByEmail(email: String): UserResponseData? {
       val user = collection.find(Filters.eq("email", email)).first()
        if (user != null){
            val userDocument = user.let(User::fromDocument)
           return UserResponseData(userId = user["_id"].toString(),name = userDocument.name, email= userDocument.email)
        }
        return null
    }

    private fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    fun verifyPassword(password: String, email: String): UserResponseData?{
        val user = collection.find(Filters.eq("email", email)).first()
        if (user != null){
            val userDocument = user.let(User::fromDocument)
            if (BCrypt.checkpw(password, userDocument.password)) {
              return  UserResponseData(userId = user["_id"].toString(),name = userDocument.name, email= userDocument.email)
            }

        }
        return null
    }

}

