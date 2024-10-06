package com.kartiknayak.easeexpense.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kartiknayak.easeexpense.MonthlyTransactionDetailsActivity
import com.kartiknayak.easeexpense.SharedFunctions
import com.kartiknayak.easeexpense.databinding.LayoutMonthlyInfoTileBinding
import com.kartiknayak.easeexpense.model.MonthlyOverviewData

class MonthlyOverviewAdapter(private var monthlyOverviewData: List<MonthlyOverviewData>) :
    RecyclerView.Adapter<MonthlyOverviewAdapter.MonthlyOverviewHolder>() {
    inner class MonthlyOverviewHolder(val binding: LayoutMonthlyInfoTileBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthlyOverviewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = LayoutMonthlyInfoTileBinding.inflate(layoutInflater, parent, false)
        return MonthlyOverviewHolder(binding)
    }

    override fun onBindViewHolder(holder: MonthlyOverviewHolder, position: Int) {
        val currentTimeOverview = monthlyOverviewData[position]
        val timeList = currentTimeOverview.time.split("-")
        val year = timeList[0]
        val month = SharedFunctions().getMonthName(timeList[1].toInt())

        holder.binding.apply {
            val transactionCount = currentTimeOverview.transactionCount
            "$year $month".also { timeTV.text = it }
            val transactionCountSuffix = if (transactionCount > 1) "Expenses" else "Expense"
            "$transactionCount $transactionCountSuffix".also { monthlyTransactionCount.text = it }
            "₹${currentTimeOverview.budget}".also { monthlyBudgetTV.text = it }
            "₹${currentTimeOverview.expense}".also { monthlyExpenseTV.text = it }
            "₹${currentTimeOverview.savings}".also { monthlySavingsTV.text = it }
            "₹${currentTimeOverview.overspent}".also { monthlyOverspentTV.text = it }

            openSpecificMonthlyTransactionsBTN.setOnClickListener {
                val intent =
                    Intent(holder.itemView.context, MonthlyTransactionDetailsActivity::class.java)
                intent.putExtra("time", currentTimeOverview.time)
                holder.itemView.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = monthlyOverviewData.size

    fun setData(monthlyOverviewData: List<MonthlyOverviewData>) {
        this.monthlyOverviewData = monthlyOverviewData
        notifyDataSetChanged()
    }
}