package com.kartiknayak.easeexpense

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import androidx.sqlite.db.SimpleSQLiteQuery
import com.google.android.material.radiobutton.MaterialRadioButton
import com.kartiknayak.easeexpense.adapter.TransactionAdapter
import com.kartiknayak.easeexpense.adapter.WishlistItemAdapter
import com.kartiknayak.easeexpense.databinding.ActivitySearchBinding
import com.kartiknayak.easeexpense.db.AppDatabase
import com.kartiknayak.easeexpense.model.Transaction
import com.kartiknayak.easeexpense.model.WishlistItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SearchActivity : AppCompatActivity() {
    private var itemId: Int = -1
    private lateinit var binding: ActivitySearchBinding

    private lateinit var db: AppDatabase
    private lateinit var transactions: List<Transaction>
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var transactionsSearchResultList: List<Transaction>

    private lateinit var wishlist: List<WishlistItem>
    private lateinit var wishlistItemAdapter: WishlistItemAdapter
    private lateinit var wishlistSearchResultList: List<WishlistItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val title = intent.getStringExtra("title")
        itemId = intent.getIntExtra("searchId", -1)

        transactionAdapter = TransactionAdapter(emptyList())
        wishlistItemAdapter = WishlistItemAdapter(emptyList())

        val dbName = if (itemId == 1) "transactions" else "wishlist"
        db = Room.databaseBuilder(baseContext, AppDatabase::class.java, dbName).build()

        binding.apply {
            tooltipTitle.text = title
            if (itemId == 2) binding.radioGroup.visibility = View.VISIBLE
            searchListRV.apply {
                adapter = if (itemId == 1) transactionAdapter else wishlistItemAdapter
                layoutManager = LinearLayoutManager(baseContext)
            }
        }
        setListeners()
    }

    private fun setListeners() {
        binding.apply {
            searchInput.addTextChangedListener {
                val query = searchInput.text.toString()

                if (itemId == 1) {
                    if (query.isEmpty()) {
                        setTransactionAdapter(transactions)
                        toggleNoResultsIVVisibility(false)
                        return@addTextChangedListener
                    }

                    transactionsSearchResultList =
                        transactions.filter { it.title.startsWith(query) }
                    setTransactionAdapter(transactionsSearchResultList)
                    toggleNoResultsIVVisibility(transactionsSearchResultList.isEmpty())
                } else {
                    if (query.isEmpty()) {
                        setWishlistAdapter(wishlist)
                        toggleNoResultsIVVisibility(false)
                        return@addTextChangedListener
                    }

                    wishlistSearchResultList = wishlist.filter { it.title.startsWith(query) }
                    setWishlistAdapter(wishlistSearchResultList)
                    toggleNoResultsIVVisibility(wishlistSearchResultList.isEmpty())
                }
            }
            radioButtonListener(allRB)
            radioButtonListener(completeRB)
            radioButtonListener(pendingRB)
            sortOrderBTN.setOnClickListener { toggleSortOrder() }
            sortByBTN.setOnClickListener { if (itemId == 1) sortTransactions() else sortWishlist() }
            backBTN.setOnClickListener { finish() }
        }
    }

    private fun toggleNoResultsIVVisibility(enable: Boolean) {
        binding.emptySearchListLayout.visibility = if (enable) View.VISIBLE else View.GONE
    }

    private fun radioButtonListener(radioButton: MaterialRadioButton) {
        radioButton.setOnClickListener { keepSortOrderIntact() }
    }

    private fun runCustomQuery(query: SimpleSQLiteQuery) {
        lifecycleScope.launch {
            if (itemId == 1) {
                transactionsSearchResultList =
                    withContext(Dispatchers.IO) { db.transactionDao().executeRawQuery(query) }
                runOnUiThread { setTransactionAdapter(transactionsSearchResultList) }

            } else {
                wishlistSearchResultList =
                    withContext(Dispatchers.IO) { db.wishlistItemDao().executeRawQuery(query) }
                runOnUiThread { setWishlistAdapter(wishlistSearchResultList) }
            }
        }
    }

    private fun getQuery(sortBy: String, searchString: String): String {
        val sortOrderTextViewValue = binding.sortOrderTV.text
        val order = if (sortOrderTextViewValue == "Ascending") "ASC" else "DESC"

        if (itemId == 1) {
            return if (searchString.isEmpty()) {
                "SELECT * FROM transactions ORDER BY $sortBy $order"
            } else {
                "SELECT * FROM transactions WHERE title LIKE '$searchString%' ORDER BY $sortBy $order"
            }
        }

        val range = binding.let {
            when {
                it.allRB.isChecked -> ""
                it.completeRB.isChecked -> "WHERE savedAmount = targetAmount"
                it.pendingRB.isChecked -> "WHERE savedAmount != targetAmount"
                else -> "WHERE"
            }
        }

        return if (searchString.isEmpty()) {
            "SELECT * FROM wishlist $range ORDER BY $sortBy $order"
        } else {
            if (range.isEmpty()) {
                "SELECT * FROM wishlist WHERE title LIKE '$searchString%' ORDER BY $sortBy $order"
            } else {
                "SELECT * FROM wishlist $range AND title LIKE '$searchString%' ORDER BY $sortBy $order"
            }
        }
    }

    private fun keepSortOrderIntact() {
        val sortOrder = binding.sortOrderTV.text.toString()
        binding.sortOrderTV.text = if (sortOrder == "Ascending") "Descending" else "Ascending"
        toggleSortOrder()
    }

    private fun toggleSortOrder() {
        val sortOrder = binding.sortOrderTV.text
        val sortByTextViewValue = binding.sortByTV.text.toString()
        val searchTerm = binding.searchInput.text.toString()

        val sortBy = if (itemId == 1) {
            binding.sortByTV.text.toString()
        } else {
            when (sortByTextViewValue) {
                "Title" -> "title"
                "Date" -> "date"
                "Target Amount" -> "targetAmount"
                "Contribution" -> "monthlyContribution"
                else -> ""
            }
        }

        binding.sortOrderTV.text = if (sortOrder == "Descending") "Ascending" else "Descending"
        val icon =
            if (sortOrder == "Descending") R.drawable.ic_sort_ascend else R.drawable.ic_sort_descend
        binding.sortOrderBTN.setImageResource(icon)

        val query = SimpleSQLiteQuery(getQuery(sortBy, searchTerm))
        runCustomQuery(query)
    }

    private fun sortTransactions() {
        val searchTerm = binding.searchInput.text.toString()
        val sortBy = when (binding.sortByTV.text.toString()) {
            "Title" -> "date".also { binding.sortByTV.text = "Date" }
            "Date" -> "amount".also { binding.sortByTV.text = "Amount" }
            "Amount" -> "title".also { binding.sortByTV.text = "Title" }
            else -> "title".also { binding.sortByTV.text = "Title" }
        }

        val query = SimpleSQLiteQuery(getQuery(sortBy, searchTerm))
        runCustomQuery(query)
    }

    private fun sortWishlist() {
        val searchTerm = binding.searchInput.text.toString()
        val sortBy = when (binding.sortByTV.text.toString()) {
            "Title" -> "date".also { binding.sortByTV.text = "Date" }
            "Date" -> "targetAmount".also { binding.sortByTV.text = "Target Amount" }
            "Target Amount" -> "monthlyContribution".also { binding.sortByTV.text = "Contribution" }
            "Contribution" -> "title".also { binding.sortByTV.text = "Title" }
            else -> "title".also { binding.sortByTV.text = "Title" }
        }

        val query = SimpleSQLiteQuery(getQuery(sortBy, searchTerm))
        runCustomQuery(query)
    }

    private fun setTransactionAdapter(currentList: List<Transaction>) {
        transactionAdapter = TransactionAdapter(currentList)
        binding.searchListRV.adapter = transactionAdapter
    }

    private fun setWishlistAdapter(currentList: List<WishlistItem>) {
        wishlistItemAdapter = WishlistItemAdapter(currentList)
        binding.searchListRV.adapter = wishlistItemAdapter
    }

    private fun fetchData() {
        lifecycleScope.launch {
            if (itemId == 1) {
                transactions = withContext(Dispatchers.IO) {
                    db.transactionDao().fetchAll().sortedByDescending { it.date }
                }
                runOnUiThread { setTransactionAdapter(transactions) }
            } else {
                wishlist = withContext(Dispatchers.IO) {
                    db.wishlistItemDao().fetchAll().reversed()
                }
                runOnUiThread { setWishlistAdapter(wishlist) }
            }
        }
    }

    private fun resetSort() {
        binding.apply {
            sortByTV.text = "Date"
            sortOrderTV.text = "Descending"
            if (searchInput.text?.isNotEmpty()!!) searchInput.setText("")
            allRB.isChecked = true
        }
    }

    override fun onResume() {
        super.onResume()
        fetchData()
        resetSort()
    }
}