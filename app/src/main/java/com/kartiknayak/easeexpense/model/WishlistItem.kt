package com.kartiknayak.easeexpense.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "wishlist")
data class WishlistItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val title: String,
    val targetAmount: Double,
    val savedAmount: Double,
    val monthlyContribution: Double,
    val date: String,
    val icon: String,
    val transactionDetails: String
) : Serializable