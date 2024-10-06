package com.kartiknayak.easeexpense.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kartiknayak.easeexpense.R
import com.kartiknayak.easeexpense.SharedFunctions
import com.kartiknayak.easeexpense.UpdateTransactionActivity
import com.kartiknayak.easeexpense.databinding.LayoutTransactionTileBinding
import com.kartiknayak.easeexpense.model.ExpenseManager
import com.kartiknayak.easeexpense.model.Transaction

class TransactionAdapter(private var transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionHolder>() {
    inner class TransactionHolder(val binding: LayoutTransactionTileBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = LayoutTransactionTileBinding.inflate(layoutInflater, parent, false)
        return TransactionHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionHolder, position: Int) {
        val transaction = transactions[position]

        val title = transaction.title
        val transactionDate = SharedFunctions().convertDateToReadableFormat(transaction.date)
        val categories = ExpenseManager().getCategories()
        val imageSrc = categories[transaction.categoryIndex].imageId
        val categoryName = categories[transaction.categoryIndex].label
        val trailingIconVisibility = if (transaction.imageData != null) View.VISIBLE else View.GONE

        val amount = if (transaction.amount >= 0) transaction.amount else -transaction.amount
        val colorResId = if (transaction.amount >= 0) R.color.green else R.color.red
        val amountText = "%s ₹%.2f".format(if (transaction.amount >= 0) "+" else "-", amount)

        holder.binding.apply {
            categoryImageIV.setImageResource(imageSrc)
            titleTV.text = title
            categoryNameTV.text = categoryName
            titleTrailingIcon.visibility = trailingIconVisibility
            amountTV.text = amountText
            amountTV.setTextColor(ContextCompat.getColor(amountTV.context, colorResId))
            transactionDateTV.text = transactionDate
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, UpdateTransactionActivity::class.java)
            intent.putExtra("transactionId", transaction.id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = transactions.size

    fun setData(transactions: List<Transaction>) {
        this.transactions = transactions
        notifyDataSetChanged()
    }
}