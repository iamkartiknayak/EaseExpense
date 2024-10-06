package com.kartiknayak.easeexpense

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kartiknayak.easeexpense.adapter.WishlistItemTransactionHistoryAdapter
import com.kartiknayak.easeexpense.databinding.ActivityWishlistItemTransactionHistoryBinding

class WishlistItemTransactionHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWishlistItemTransactionHistoryBinding

    private lateinit var wishlistItemTransactionHistoryAdapter: WishlistItemTransactionHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityWishlistItemTransactionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val transactionDetails = intent.getStringExtra("transactionDetails")
        wishlistItemTransactionHistoryAdapter =
            WishlistItemTransactionHistoryAdapter(
                transactionDetails!!.split(",").reversed()
            )

        binding.apply {
            backBTN.setOnClickListener { finish() }
            wishlistItemTransactionHistoryRV.apply {
                adapter = wishlistItemTransactionHistoryAdapter
                layoutManager = LinearLayoutManager(baseContext)
            }
        }
    }
}