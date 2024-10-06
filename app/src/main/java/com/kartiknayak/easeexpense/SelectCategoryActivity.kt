package com.kartiknayak.easeexpense

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.kartiknayak.easeexpense.adapter.CategoryAdapter
import com.kartiknayak.easeexpense.databinding.ActivitySelectCategoryBinding
import com.kartiknayak.easeexpense.model.ExpenseCategory
import com.kartiknayak.easeexpense.model.ExpenseManager

class SelectCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectCategoryBinding

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var categories: List<ExpenseCategory>
    private lateinit var searchResultList: List<ExpenseCategory>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySelectCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categories = ExpenseManager().getCategories()
        searchResultList = emptyList()
        setCategoryAdapter(categories)
        setListeners()
    }

    private fun setListeners() {
        binding.apply {
            backBTN.setOnClickListener { finish() }
            searchInput.addTextChangedListener { setSearchResult() }
            categoryGridView.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    val intent = Intent()

                    if (searchResultList.isEmpty()) {
                        intent.putExtra("categoryIndex", position)
                    } else {
                        val selectedLabel = searchResultList[position].label
                        val actualIndex = categories.indexOfFirst { it.label == selectedLabel }
                        intent.putExtra("categoryIndex", actualIndex)
                    }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
        }
    }

    private fun setSearchResult() {
        binding.apply {
            val query = searchInput.text.toString()

            if (query.isEmpty()) {
                setCategoryAdapter(categories)
                return
            }
            searchResultList = categories.filter { it.label.startsWith(query) }
            setCategoryAdapter(searchResultList)
        }
    }

    private fun setCategoryAdapter(updatedList: List<ExpenseCategory>) {
        categoryAdapter = CategoryAdapter(updatedList)
        binding.categoryGridView.adapter = categoryAdapter
    }

    fun makeIntent(context: Context?): Intent {
        return Intent(context, SelectCategoryActivity::class.java)
    }
}