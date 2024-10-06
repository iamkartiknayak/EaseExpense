package com.kartiknayak.easeexpense

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.kartiknayak.easeexpense.adapter.MonthlyOverviewAdapter
import com.kartiknayak.easeexpense.databinding.ActivityTransactionsHistoryBinding
import com.kartiknayak.easeexpense.db.AppDatabase
import com.kartiknayak.easeexpense.model.MonthlyOverviewData
import com.kartiknayak.easeexpense.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class TransactionsHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionsHistoryBinding

    private lateinit var db: AppDatabase
    private lateinit var transactions: List<Transaction>
    private lateinit var monthlyOverviewData: List<MonthlyOverviewData>

    private lateinit var monthlyOverviewAdapter: MonthlyOverviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTransactionsHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transactions = mutableListOf()
        monthlyOverviewData = mutableListOf()
        monthlyOverviewAdapter = MonthlyOverviewAdapter(monthlyOverviewData)
        db = Room.databaseBuilder(baseContext, AppDatabase::class.java, "transactions").build()

        binding.apply {
            monthlyOverviewRV.apply {
                adapter = monthlyOverviewAdapter
                layoutManager = LinearLayoutManager(baseContext)
            }
            backBTN.setOnClickListener { finish() }
        }
    }

    private fun setMonthlyOverviewAdapterData() {
        val listOfTime = transactions.map { it.date.substring(0, 7) }.distinct()

        monthlyOverviewData = listOfTime.map { time ->
            val transactionsForTime = transactions.filter { it.date.substring(0, 7) == time }
            val budget = transactionsForTime.filter { it.amount > 0 }.sumOf { it.amount }
            val expense = transactionsForTime.filter { it.amount < 0 }.sumOf { it.amount }
            val savings = (budget - abs(expense)).takeIf { it > 0 } ?: 0.0
            val overspent = (budget - abs(expense)).takeIf { it < 0 } ?: 0.0
            val transactionCount = transactionsForTime.count { it.amount < 0 }

            MonthlyOverviewData(
                time,
                transactionCount,
                budget,
                expense,
                savings,
                overspent
            )
        }
        monthlyOverviewAdapter.setData(monthlyOverviewData.sortedByDescending { it.time })
    }

    private fun fetchAll() {
        lifecycleScope.launch {
            transactions = withContext(Dispatchers.IO) { db.transactionDao().fetchAll().reversed() }
            runOnUiThread { setMonthlyOverviewAdapterData() }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchAll()
    }
}