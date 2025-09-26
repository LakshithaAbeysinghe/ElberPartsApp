package com.elber.parts.data.db

import androidx.room.*

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): User?

    @Query("UPDATE users SET password_hash = :hash WHERE email = :email")
    suspend fun updatePassword(email: String, hash: String): Int
}
