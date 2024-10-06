package com.kartiknayak.easeexpense.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val title: String,
    val amount: Double,
    val date: String,
    val categoryIndex: Int,
    val imageData: ByteArray?,
    val description: String,
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Transaction

        if (id != other.id) return false
        if (title != other.title) return false
        if (amount != other.amount) return false
        if (date != other.date) return false
        if (categoryIndex != other.categoryIndex) return false
        if (imageData != null) {
            if (other.imageData == null) return false
            if (!imageData.contentEquals(other.imageData)) return false
        } else if (other.imageData != null) return false
        if (description != other.description) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + title.hashCode()
        result = 31 * result + amount.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + categoryIndex
        result = 31 * result + (imageData?.contentHashCode() ?: 0)
        result = 31 * result + description.hashCode()
        return result
    }
}
