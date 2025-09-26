package com.elber.parts.data.db

import androidx.room.*

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo val email: String,
    @ColumnInfo(name = "password_hash") val passwordHash: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
