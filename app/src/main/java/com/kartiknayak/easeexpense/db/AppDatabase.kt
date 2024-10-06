package com.kartiknayak.easeexpense.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kartiknayak.easeexpense.model.Transaction
import com.kartiknayak.easeexpense.model.WishlistItem

@Database(entities = [Transaction::class, WishlistItem::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun wishlistItemDao(): WishlistItemDao
}