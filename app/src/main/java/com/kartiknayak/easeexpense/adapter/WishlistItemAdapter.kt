package com.kartiknayak.easeexpense.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kartiknayak.easeexpense.SharedFunctions
import com.kartiknayak.easeexpense.WishlistItemDetailsActivity
import com.kartiknayak.easeexpense.databinding.LayoutWishlistTileBinding
import com.kartiknayak.easeexpense.model.WishlistItem

class WishlistItemAdapter(private var wishList: List<WishlistItem>) :
    RecyclerView.Adapter<WishlistItemAdapter.WishlistItemHolder>() {
    inner class WishlistItemHolder(val binding: LayoutWishlistTileBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishlistItemHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = LayoutWishlistTileBinding.inflate(layoutInflater, parent, false)
        return WishlistItemHolder(binding)
    }

    override fun onBindViewHolder(holder: WishlistItemHolder, position: Int) {
        val wishlistItem = wishList[position]

        val title = wishlistItem.title
        val targetAmount = wishlistItem.targetAmount
        val savedAmount = wishlistItem.savedAmount
        val monthlyContribution = wishlistItem.monthlyContribution
        val dateCreated = SharedFunctions().convertDateToReadableFormat(wishlistItem.date)
        val icon = wishlistItem.icon
        val savedPercent = ((savedAmount / targetAmount) * 100).toInt()

        holder.binding.apply {
            wishlistTitleTV.text = title
            "Saving ₹%.0f/mon".format(monthlyContribution).also { monthlyContributionTV.text = it }
            "%d%%".format(savedPercent).also { savedPercentTV.text = it }
            "To save ₹%.0f".format(targetAmount).also { targetAmountTV.text = it }
            "%s".format(dateCreated).also { wishlistAddedDateTV.text = it }

            categoryIconTV.text = if (icon.isEmpty()) "\uD83E\uDD14" else wishlistItem.icon

            val progressIndicator = savedTargetAmountIndicator
            progressIndicator.setProgress(savedPercent, true)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, WishlistItemDetailsActivity::class.java)
            intent.putExtra("wishlistItem", wishlistItem)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = wishList.size

    fun setData(wishList: List<WishlistItem>) {
        this.wishList = wishList
        notifyDataSetChanged()
    }
}