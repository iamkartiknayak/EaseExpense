package com.kartiknayak.easeexpense

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.kartiknayak.easeexpense.databinding.ActivityAddTransactionBinding
import com.kartiknayak.easeexpense.db.AppDatabase
import com.kartiknayak.easeexpense.model.ExpenseCategory
import com.kartiknayak.easeexpense.model.ExpenseManager
import com.kartiknayak.easeexpense.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var imagePicker: ActivityResultLauncher<PickVisualMediaRequest>

    private lateinit var categories: List<ExpenseCategory>
    private var categoryIndex: Int? = null

    private var sharedFunctions = SharedFunctions()
    private var imageByteArray: ByteArray? = null
    private var selectedDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedFunctions = SharedFunctions()
        categories = ExpenseManager().getCategories()
        setListeners()
    }

    private fun setListeners() {
        binding.apply {
            main.setOnClickListener {
                sharedFunctions.removeFocusFromEditTexts(
                    addTransactionEditTextControlsParentLayout,
                    main,
                    baseContext
                )
            }
            sharedFunctions.updateEditTextLayoutStatus(titleInput, titleInputLayout)
            sharedFunctions.updateEditTextLayoutStatus(amountInput, amountInputLayout)
            sharedFunctions.updateEditTextLayoutStatus(dateInput, dateInputLayout)

            backBTN.setOnClickListener { finish() }
            // TODO: MIGRATE METHOD
            categoryIconIV.setOnClickListener {
                val intent = SelectCategoryActivity().makeIntent(baseContext)
                startActivityForResult(intent, 1020)
            }
            imagePicker = sharedFunctions.getImagePicker(
                root.context as ComponentActivity,
                { byteArray -> imageByteArray = byteArray },
                {
                    attachImageTV.text = getString(R.string.view_image)
                    deleteAttachedImageIV.visibility = View.VISIBLE
                    attachImageIV.setImageResource(R.drawable.ic_view_image)
                }
            )
            incomeRB.setOnClickListener { expenseRB.clearFocus() }
            expenseRB.setOnClickListener { incomeRB.clearFocus() }
            attachImageIV.setOnClickListener {
                sharedFunctions.pickOrViewImage(imageByteArray, baseContext, imagePicker)
            }
            deleteAttachedImageIV.setOnClickListener { deleteImage() }
            dateInput.setOnClickListener {
                sharedFunctions.showDatePickerDialog(
                    it.context,
                    dateInput
                ) { selectedDay -> selectedDate = selectedDay }
            }
            addTransactionBTN.setOnClickListener { addTransaction() }
        }
    }

    private fun deleteImage() {
        binding.apply {
            imageByteArray = null
            attachImageIV.setImageResource(R.drawable.id_add_image)
            attachImageTV.text = "Add Image"
            deleteAttachedImageIV.visibility = View.GONE
        }
    }

    private fun addTransaction() {
        binding.apply {
            val transactionTitle = titleInput.text.toString()
            val transactionAmount = amountInput.text.toString().toDoubleOrNull()
            val transactionDate = selectedDate
            val transactionCategory = categoryTitleTV.text
            val transactionImageData = imageByteArray
            val transactionDescription = descriptionInput.text.toString()

            val transactionCategoryIndex =
                categories.indexOf(categories.find { it.label == transactionCategory })

            if (transactionCategoryIndex == -1) {
                sharedFunctions.showToast(
                    baseContext,
                    "Transaction category is not selected"
                )
            } else if (!incomeRB.isChecked && !expenseRB.isChecked) {
                sharedFunctions.showToast(baseContext, "Transaction type is not selected")
            } else if (transactionTitle.isEmpty()) {
                titleInputLayout.error = "Please enter a valid title"
            } else if (transactionAmount == null || transactionAmount == 0.0) {
                amountInputLayout.error = "Please enter a valid amount"
            } else if (transactionDate == null) {
                dateInputLayout.error = "Please enter a valid date"
            } else {
                val signedAmount =
                    if (incomeRB.isChecked) transactionAmount else -transactionAmount
                val transaction =
                    Transaction(
                        0,
                        transactionTitle,
                        signedAmount,
                        transactionDate,
                        transactionCategoryIndex,
                        transactionImageData,
                        transactionDescription,
                    )
                insert(transaction)
            }
        }
    }

    @Deprecated("Deprecated in Java") // TODO: MIGRATE METHOD
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1020 && resultCode == Activity.RESULT_OK) {
            categoryIndex = data?.getIntExtra("categoryIndex", -1)

            if (categoryIndex != -1) {
                binding.apply {
                    val categoryIcon = categories[categoryIndex!!].imageId
                    val categoryTitle = categories[categoryIndex!!].label
                    categoryIconIV.setImageResource(categoryIcon)
                    categoryTitleTV.text = categoryTitle

                    if (categoryIndex == 2)
                        incomeRB.isChecked = true else expenseRB.isChecked = true
                }
            }
        } else {
            Log.i("MyApp", "Cancelled")
        }
    }

    private fun insert(transaction: Transaction) {
        val db = Room.databaseBuilder(baseContext, AppDatabase::class.java, "transactions").build()
        lifecycleScope.launch {
            withContext(Dispatchers.IO) { db.transactionDao().insertAll(transaction) }
            finish()
        }
    }
}