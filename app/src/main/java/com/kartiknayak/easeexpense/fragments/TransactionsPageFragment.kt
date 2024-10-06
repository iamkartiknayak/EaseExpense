package com.kartiknayak.easeexpense.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.kartiknayak.easeexpense.AddTransactionActivity
import com.kartiknayak.easeexpense.FloatingButtonAction
import com.kartiknayak.easeexpense.R
import com.kartiknayak.easeexpense.SearchActivity
import com.kartiknayak.easeexpense.SharedFunctions
import com.kartiknayak.easeexpense.SplitExpenseActivity
import com.kartiknayak.easeexpense.TransactionsHistoryActivity
import com.kartiknayak.easeexpense.adapter.TopSpendingCategoryAdapter
import com.kartiknayak.easeexpense.adapter.TransactionAdapter
import com.kartiknayak.easeexpense.databinding.FragmentTransactionsPageBinding
import com.kartiknayak.easeexpense.db.AppDatabase
import com.kartiknayak.easeexpense.model.TopSpendingCategory
import com.kartiknayak.easeexpense.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class TransactionsPageFragment : Fragment() {
    private lateinit var binding: FragmentTransactionsPageBinding
    private lateinit var sharedFunctions: SharedFunctions

    private lateinit var db: AppDatabase
    private lateinit var transactions: List<Transaction>
    private lateinit var oldTransactions: List<Transaction>
    private lateinit var deletedTransaction: Transaction
    private lateinit var recentTransactionAdapter: TransactionAdapter
    private lateinit var previousTransactionAdapter: TransactionAdapter

    private lateinit var topSpendingCategories: List<TopSpendingCategory>
    private lateinit var topSpendingCategoryAdapter: TopSpendingCategoryAdapter

    private var floatingButtonAction = FloatingButtonAction.OPEN_ADD_TRANSACTION_PAGE

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentTransactionsPageBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedFunctions = SharedFunctions()
        recentTransactionAdapter = TransactionAdapter(emptyList())
        previousTransactionAdapter = TransactionAdapter(emptyList())
        topSpendingCategoryAdapter = TopSpendingCategoryAdapter(emptyList())
        db = Room.databaseBuilder(requireContext(), AppDatabase::class.java, "transactions").build()

        setRecyclerViews()
        setListeners()
    }

    private fun setRecyclerViews() {
        binding.apply {
            topSpendingRV.apply {
                adapter = topSpendingCategoryAdapter
                layoutManager = LinearLayoutManager(
                    requireContext(), LinearLayoutManager.HORIZONTAL, false
                )
            }

            recentTransactionsRV.apply {
                adapter = recentTransactionAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }

            previousTransactionsRV.apply {
                adapter = previousTransactionAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
        }
    }

    private fun setListeners() {
        binding.apply {
            val itemTouchHelper = sharedFunctions.getItemTouchHelper { position ->
                deleteTransaction(transactions[position])
            }
            itemTouchHelper.attachToRecyclerView(recentTransactionsRV)

            openTransactionsHistoryPageBTN.setOnClickListener {
                val intent = Intent(requireContext(), TransactionsHistoryActivity::class.java)
                startActivity(intent)
            }

            openSplitTransactionPageBTN.setOnClickListener {
                val intent = Intent(requireContext(), SplitExpenseActivity::class.java)
                startActivity(intent)
            }

            openSearchTransactionPageBTN.setOnClickListener {
                val intent = Intent(requireContext(), SearchActivity::class.java)
                intent.putExtra("searchId", 1)
                intent.putExtra("title", "Transactions")
                startActivity(intent)
            }

            floatingActionButton.setOnClickListener {
                when (floatingButtonAction) {
                    FloatingButtonAction.OPEN_ADD_TRANSACTION_PAGE -> {
                        val intent = Intent(requireContext(), AddTransactionActivity::class.java)
                        startActivity(intent)
                    }

                    FloatingButtonAction.SCROLL_UP -> scrollView.smoothScrollTo(0, 0)
                }
            }

            scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
                val icon = if (scrollY > 150) {
                    floatingButtonAction = FloatingButtonAction.SCROLL_UP
                    R.drawable.ic_double_chevron_up
                } else {
                    floatingButtonAction = FloatingButtonAction.OPEN_ADD_TRANSACTION_PAGE
                    R.drawable.ic_add
                }
                floatingActionButton.setImageResource(icon)
            }
        }
    }

    private fun updateUI() {
        updateTransactionsDashboard()
        updateTopSpendingSection()

        val currentMonth = sharedFunctions.getCurrentMonth()
        transactions = transactions.sortedByDescending { it.date }
        val recentTransactions = transactions.filter { it.date.split("-")[1] == currentMonth }
        val previousTransactions = transactions.filter { it.date.split("-")[1] != currentMonth }

        binding.recentTransactionsLayout.visibility =
            if (recentTransactions.isNotEmpty()) View.VISIBLE else View.GONE

        binding.previousTransactionsLayout.visibility =
            if (previousTransactions.isNotEmpty()) View.VISIBLE else View.GONE

        recentTransactionAdapter.setData(recentTransactions)
        previousTransactionAdapter.setData(previousTransactions)
        topSpendingCategoryAdapter.setData(topSpendingCategories)

        binding.apply {
            openSearchTransactionPageBTN.visibility =
                if (transactions.size < 3) View.GONE else View.VISIBLE

            openTransactionsHistoryPageBTN.visibility =
                if (transactions.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun updateTopSpendingSection() {
        val currentMonth = sharedFunctions.getCurrentMonth()
        val categoryIndexMap = transactions
            .filter { it.date.split("-")[1] == currentMonth && it.amount.toInt() < 0 }
            .groupingBy { it.categoryIndex }
            .fold(listOf(0, 0)) { acc, transaction ->
                listOf(acc[0] + 1, acc[1] + transaction.amount.toInt())
            }

        topSpendingCategories = categoryIndexMap.map { (index, values) ->
            TopSpendingCategory(index, values[0], values[1])
        }.sortedBy { it.transactionAmount }

        binding.topSpendingLayout.visibility =
            if (topSpendingCategories.size > 3) View.VISIBLE else View.GONE
    }

    private fun updateTransactionsDashboard() {
        val currentMonth = sharedFunctions.getCurrentMonth()
        val totalAmount = transactions.filter { it.date.split("-")[1] == currentMonth }
            .sumOf { it.amount }
        val budgetAmount =
            transactions.filter { it.date.split("-")[1] == currentMonth && it.amount > 0 }
                .sumOf { it.amount }
        val expenseAmount = totalAmount - budgetAmount
        var spentPercent = abs((expenseAmount / budgetAmount) * 100).toInt()
        if (spentPercent > 100) spentPercent = 100

        binding.apply {
            "₹ %.2f".format(totalAmount).also { balanceTV.text = it }
            "₹ %.2f".format(budgetAmount).also { budgetTV.text = it }
            "₹ %.2f".format(abs(expenseAmount)).also { expenseTV.text = it }
            "%d%%".format(abs(spentPercent)).also { spentPercentTV.text = it }
            "₹ %.0f".format(budgetAmount).also { budgetTV2.text = it }

            emptyTransactionImageLayout.visibility =
                if (transactions.isEmpty()) View.VISIBLE else View.GONE
            monthlyExpenseIndicator.setProgress(spentPercent, true)
        }
    }

    private fun fetchAll() {
        lifecycleScope.launch {
            transactions = withContext(Dispatchers.IO) { db.transactionDao().fetchAll().reversed() }
            requireActivity().runOnUiThread { updateUI() }
        }
    }

    private fun undoDelete() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) { db.transactionDao().insertAll(deletedTransaction) }
            transactions = oldTransactions
            requireActivity().runOnUiThread { updateUI() }
        }
    }

    private fun deleteTransaction(transaction: Transaction) {
        deletedTransaction = transaction
        oldTransactions = transactions
        transactions = transactions.filter { it.id != transaction.id }

        lifecycleScope.launch {
            withContext(Dispatchers.IO) { db.transactionDao().delete(transaction) }
            requireActivity().runOnUiThread {
                updateUI()
                sharedFunctions.showSnackbar(
                    "Transaction",
                    binding,
                    requireContext()
                ) { undoDelete() }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchAll()
    }
}