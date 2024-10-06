package com.kartiknayak.easeexpense.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.kartiknayak.easeexpense.model.Transaction

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions")
    fun fetchAll(): List<Transaction>

    @Insert
    fun insertAll(vararg transaction: Transaction)

    @Delete
    fun delete(transaction: Transaction)

    @Update
    fun update(vararg transaction: Transaction)

    @RawQuery(observedEntities = [Transaction::class])
    fun executeRawQuery(query: SupportSQLiteQuery): List<Transaction>

    @Query("SELECT * FROM transactions WHERE id = :transactionID")
    fun getTransaction(transactionID: Int): Transaction
}