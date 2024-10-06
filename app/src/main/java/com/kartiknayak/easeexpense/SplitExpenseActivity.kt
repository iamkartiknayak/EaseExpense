package com.kartiknayak.easeexpense

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.kartiknayak.easeexpense.databinding.ActivitySplitExpenseBinding

class SplitExpenseActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplitExpenseBinding

    private lateinit var sharedFunctions: SharedFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySplitExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListeners()
    }

    private fun setListeners() {
        sharedFunctions = SharedFunctions()
        binding.apply {
            main.setOnClickListener {
                sharedFunctions.removeFocusFromEditTexts(
                    splitExpenseEditTextControlsParentLayout,
                    main,
                    baseContext
                )
            }
            sharedFunctions.updateEditTextLayoutStatus(titleInput, titleInputLayout) {
                toggleShareButtonVisibility(titleInput)
            }
            sharedFunctions.updateEditTextLayoutStatus(amountInput, amountInputLayout) {
                toggleShareButtonVisibility(amountInput)
            }
            sharedFunctions.updateEditTextLayoutStatus(noOfPeopleInput, noOfPeopleInputLayout) {
                toggleShareButtonVisibility(noOfPeopleInput)
            }
            sharedFunctions.updateEditTextLayoutStatus(tipAmountInput, tipAmountInputLayout) {
                toggleShareButtonVisibility(tipAmountInput)
            }
            shareExpenseBTN.setOnClickListener { shareExpense() }
            calculateShareBTN.setOnClickListener { calculateShare() }
            backBTN.setOnClickListener { finish() }
        }
    }

    private fun toggleShareButtonVisibility(editText: TextInputEditText) {
        if (editText.text.toString().isEmpty()) {
            binding.individualShareLayout.visibility = View.GONE
        }
    }

    private fun calculateShare() {
        binding.apply {
            val title = titleInput.text.toString()
            val amount = amountInput.text.toString().toIntOrNull()
            val noOfPeople = noOfPeopleInput.text.toString().toIntOrNull()
            val tipAmount = tipAmountInput.text.toString().toIntOrNull()

            if (title.isEmpty()) {
                titleInputLayout.error = "Please enter a valid title"
            } else if (amount == 0 || amount == null) {
                amountInputLayout.error = "Please enter a valid amount"
            } else if (noOfPeople == 0 || noOfPeople == null) {
                noOfPeopleInputLayout.error = "Please enter a valid number"
            } else if (tipAmount == null) {
                tipAmountInputLayout.error = "Please enter a valid tip amount"
            } else {
                val individualShare = (amount + tipAmount) / noOfPeople
                individualShareLayout.visibility = View.VISIBLE
                "₹$individualShare".also { individualShareTV.text = it }
            }
        }
    }

    private fun getNumber(editable: TextInputEditText): Int {
        return editable.text.toString().toInt()
    }

    private fun shareExpense() {
        val title = binding.titleInput.text.toString()
        val amount = getNumber(binding.amountInput)
        val people = getNumber(binding.noOfPeopleInput)
        val tip = getNumber(binding.tipAmountInput)
        val share = (amount + tip) / people

        val message = """
            Title     : $title
            Total    : $amount
            People : $people
            Tip         : $tip
            Share    : $share
        """.trimIndent()

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }
}