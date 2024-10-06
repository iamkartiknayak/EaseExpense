package com.kartiknayak.easeexpense.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kartiknayak.easeexpense.SharedFunctions
import com.kartiknayak.easeexpense.databinding.LayoutWishlistItemTransactionHistoryTileBinding

class WishlistItemTransactionHistoryAdapter(private var wishListItemTransactionHistory: List<String>) :
    RecyclerView.Adapter<WishlistItemTransactionHistoryAdapter.WishlistItemTransactionHolder>() {
    inner class WishlistItemTransactionHolder(val binding: LayoutWishlistItemTransactionHistoryTileBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WishlistItemTransactionHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding =
            LayoutWishlistItemTransactionHistoryTileBinding.inflate(
                layoutInflater, parent, false
            )
        return WishlistItemTransactionHolder(binding)
    }

    override fun onBindViewHolder(holder: WishlistItemTransactionHolder, position: Int) {
        val sharedFunctions = SharedFunctions()
        val currentTransaction = wishListItemTransactionHistory[position]
        val dateTime = currentTransaction.split("|").first()
        Log.e("TESTING", wishListItemTransactionHistory.toString())
        val date = sharedFunctions.convertDateToReadableFormat(dateTime)
        val time = sharedFunctions.getFormattedTime(dateTime)

        holder.binding.apply {
            transactionDateTV.text = date
            transactionTimeTV.text = time
//            transactionAmountTV.text = dateTime
            "-₹${currentTransaction.split("|").last()}".also { transactionAmountTV.text = it }
        }
    }

    override fun getItemCount(): Int = wishListItemTransactionHistory.size


}