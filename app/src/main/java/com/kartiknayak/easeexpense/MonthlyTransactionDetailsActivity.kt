package com.kartiknayak.easeexpense

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.kartiknayak.easeexpense.adapter.TransactionAdapter
import com.kartiknayak.easeexpense.databinding.ActivityMonthlyTransactionDetailsBinding
import com.kartiknayak.easeexpense.db.AppDatabase
import com.kartiknayak.easeexpense.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MonthlyTransactionDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMonthlyTransactionDetailsBinding

    private lateinit var db: AppDatabase
    private lateinit var transactions: List<Transaction>
    private lateinit var transactionAdapter: TransactionAdapter

    private lateinit var timeString: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMonthlyTransactionDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        timeString = intent.getStringExtra("time")!!
        transactionAdapter = TransactionAdapter(emptyList())
        db = Room.databaseBuilder(baseContext, AppDatabase::class.java, "transactions").build()

        val timeList = timeString.split("-")
        val monthName = SharedFunctions().getMonthName(timeList[1].toInt())

        binding.apply {
            "${timeList[0]} $monthName".also { timeTV.text = it }
            specificMonthlyTransactionsRV.apply {
                adapter = transactionAdapter
                layoutManager = LinearLayoutManager(baseContext)
            }
            backBTN.setOnClickListener { finish() }
        }
    }

    private fun fetchAll() {
        lifecycleScope.launch {
            transactions = withContext(Dispatchers.IO) { db.transactionDao().fetchAll().reversed() }
            transactions = transactions.filter { it.date.startsWith(timeString) }
                .sortedByDescending { it.date }
            runOnUiThread { transactionAdapter.setData(transactions) }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchAll()
    }
}