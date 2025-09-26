package com.elber.parts.data.repo

import android.content.Context
import com.elber.parts.data.db.AppDatabase
import com.elber.parts.data.db.User
import com.elber.parts.util.Security
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository private constructor(context: Context) {
    private val dao = AppDatabase.getInstance(context).userDao()

    companion object {
        fun from(context: Context) = UserRepository(context.applicationContext)
    }

    suspend fun signUp(email: String, rawPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (dao.getByEmail(email) != null)
            return@withContext Result.failure(IllegalStateException("Email already exists"))
        dao.insert(User(email = email, passwordHash = Security.hash(rawPassword)))
        Result.success(Unit)
    }

    suspend fun login(email: String, rawPassword: String): Boolean = withContext(Dispatchers.IO) {
        val u = dao.getByEmail(email) ?: return@withContext false
        u.passwordHash == Security.hash(rawPassword)
    }

    suspend fun emailExists(email: String): Boolean = withContext(Dispatchers.IO) {
        dao.getByEmail(email) != null
    }

    suspend fun resetPassword(email: String, rawPassword: String): Boolean = withContext(Dispatchers.IO) {
        dao.updatePassword(email, Security.hash(rawPassword)) > 0
    }
}
