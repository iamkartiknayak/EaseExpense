package com.kartiknayak.easeexpense.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kartiknayak.easeexpense.SharedFunctions
import com.kartiknayak.easeexpense.databinding.LayoutTopSpendingCardBinding
import com.kartiknayak.easeexpense.model.ExpenseManager
import com.kartiknayak.easeexpense.model.TopSpendingCategory

class TopSpendingCategoryAdapter(private var topSpendingCategories: List<TopSpendingCategory>) :
    RecyclerView.Adapter<TopSpendingCategoryAdapter.TopSpendingCategoryHolder>() {
    inner class TopSpendingCategoryHolder(val binding: LayoutTopSpendingCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopSpendingCategoryHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = LayoutTopSpendingCardBinding.inflate(layoutInflater, parent, false)
        return TopSpendingCategoryHolder(binding)
    }

    override fun onBindViewHolder(holder: TopSpendingCategoryHolder, position: Int) {
        val topSpendingCategory = topSpendingCategories[position]
        val categories = ExpenseManager().getCategories()
        val categoryIndex = topSpendingCategory.index

        val transactionAmount = topSpendingCategory.transactionAmount
        val totalTransactions = topSpendingCategories.sumOf { it.transactionAmount }
        val totalTransactionCount = topSpendingCategories.sumOf { it.transactionCount }
        val percent = (transactionAmount.toDouble() / totalTransactions) * 100

        holder.binding.apply {
            categoryImageIV.setImageResource(categories[categoryIndex].imageId)
            "%.1f%%".format(percent).also { labelTV.text = it }
        }

        holder.itemView.setOnClickListener {
            SharedFunctions().showToast(holder.itemView.context, "$totalTransactionCount")
        }
    }

    override fun getItemCount(): Int = topSpendingCategories.size

    fun setData(topSpendingCategories: List<TopSpendingCategory>) {
        this.topSpendingCategories = topSpendingCategories
        notifyDataSetChanged()
    }
}