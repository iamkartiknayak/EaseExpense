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
import com.kartiknayak.easeexpense.databinding.ActivityUpdateTransactionBinding
import com.kartiknayak.easeexpense.db.AppDatabase
import com.kartiknayak.easeexpense.model.ExpenseCategory
import com.kartiknayak.easeexpense.model.ExpenseManager
import com.kartiknayak.easeexpense.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class UpdateTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateTransactionBinding

    private lateinit var imagePicker: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var categories: List<ExpenseCategory>

    private lateinit var db: AppDatabase
    private lateinit var transaction: Transaction

    private var sharedFunctions = SharedFunctions()
    private var transactionId: Int? = null
    private var categoryIndex: Int? = null
    private var imageByteArray: ByteArray? = null
    private var selectedDate: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityUpdateTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = Room.databaseBuilder(baseContext, AppDatabase::class.java, "transactions").build()
        transactionId = intent.getIntExtra("transactionId", -1)
        setTransaction()

        sharedFunctions = SharedFunctions()
        categories = ExpenseManager().getCategories()

        setListeners()
    }

    private fun setListeners() {
        binding.apply {
            main.setOnClickListener {
                sharedFunctions.removeFocusFromEditTexts(
                    updateTransactionEditTextControlsParentLayout,
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
            updateTransactionBTN.setOnClickListener { updateTransaction() }
        }
    }

    private fun updateTransaction() {
        binding.apply {
            val transactionTitle = titleInput.text.toString()
            val transactionAmount = amountInput.text.toString().toDoubleOrNull()
            val transactionDate = selectedDate
            val transactionCategory = categoryTitleTV.text.toString()

            val transactionDescription = descriptionInput.text.toString()

            val transactionCategoryIndex =
                categories.indexOf(categories.find { it.label == transactionCategory })

            if (transactionTitle.isEmpty()) {
                titleInputLayout.error = "Please enter a valid title"
            } else if (transactionAmount == null || transactionAmount == 0.0) {
                amountInputLayout.error = "Please enter a valid amount"
            } else if (transactionDate == null) {
                dateInputLayout.error = "Please enter a valid amount"
            } else {
                val signedAmount =
                    if (incomeRB.isChecked) abs(transactionAmount) else -transactionAmount
                val transaction = Transaction(
                    transaction.id,
                    transactionTitle,
                    signedAmount,
                    transactionDate,
                    transactionCategoryIndex,
                    imageByteArray,
                    transactionDescription,
                )
                update(transaction)
            }
        }
    }

    private fun setInitialData() {
        binding.apply {
            titleInput.setText(transaction.title)
            amountInput.setText(abs(transaction.amount.toInt()).toString())
            selectedDate = transaction.date
            dateInput.setText(sharedFunctions.convertDateToReadableFormat(transaction.date))
            categoryTitleTV.text = categories[transaction.categoryIndex].label
            categoryIconIV.setImageResource(categories[transaction.categoryIndex].imageId)
            descriptionInput.setText(transaction.description)

            if (transaction.amount < 0) expenseRB.isChecked = true else incomeRB.isChecked = true

            if (transaction.imageData != null) {
                imageByteArray = transaction.imageData
                attachImageTV.text = getString(R.string.view_image)
                attachImageIV.setImageResource(R.drawable.ic_view_image)
                deleteAttachedImageIV.visibility = View.VISIBLE
            }
        }
    }

    private fun deleteImage() {
        binding.apply {
            imageByteArray = null
            attachImageIV.setImageResource(R.drawable.id_add_image)
            attachImageTV.text = getString(R.string.add_image)
            deleteAttachedImageIV.visibility = View.GONE
        }
    }

    @Deprecated("Deprecated in Java") // TODO: MIGRATE SOON
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

    private fun update(transaction: Transaction) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) { db.transactionDao().update(transaction) }
            finish()
        }
    }

    private fun setTransaction() {
        lifecycleScope.launch {
            transaction =
                withContext(Dispatchers.IO) { db.transactionDao().getTransaction(transactionId!!) }
            runOnUiThread { setInitialData() }
        }
    }
}
