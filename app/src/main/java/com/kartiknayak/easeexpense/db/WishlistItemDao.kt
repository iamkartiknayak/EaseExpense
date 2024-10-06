package com.kartiknayak.easeexpense.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.kartiknayak.easeexpense.model.WishlistItem

@Dao
interface WishlistItemDao {
    @Query("SELECT * FROM wishlist")
    fun fetchAll(): List<WishlistItem>

    @Insert
    fun insertAll(vararg wishlistItem: WishlistItem)

    @Delete
    fun delete(wishlistItem: WishlistItem)

    @Update
    fun update(vararg wishlistItem: WishlistItem)

    @RawQuery(observedEntities = [WishlistItem::class])
    fun executeRawQuery(query: SupportSQLiteQuery): List<WishlistItem>
}