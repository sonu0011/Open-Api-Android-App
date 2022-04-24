package com.sonu.openapi.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sonu.openapi.models.AccountProperties
import com.sonu.openapi.models.AuthToken
import com.sonu.openapi.models.BlogPost

@Database(
    entities = [AuthToken::class, AccountProperties::class, BlogPost::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getAuthTokenDao(): AuthTokenDao

    abstract fun getAccountPropertiesDao(): AccountPropertiesDao

    abstract fun getBlogPostDao(): BlogPostDao

    companion object {
        const val DATABASE_NAME: String = "app_db"
    }
}