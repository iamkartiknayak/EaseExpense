package com.kartiknayak.easeexpense

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.kartiknayak.easeexpense.databinding.ActivityWishlistItemDetailsBinding
import com.kartiknayak.easeexpense.db.AppDatabase
import com.kartiknayak.easeexpense.model.WishlistItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class WishlistItemDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWishlistItemDetailsBinding

    private lateinit var wishlistItem: WishlistItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityWishlistItemDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        wishlistItem = intent.getSerializableExtra("wishlistItem") as WishlistItem
        setInitialData()
        addListeners()
    }

    private fun setInitialData() {
        binding.apply {
            if (wishlistItem.targetAmount == wishlistItem.savedAmount) {
                contributionInputLayout.hint = "Contributed"
                contributionInput.setText(wishlistItem.targetAmount.toString())
                contributionInput.isEnabled = false
            }

            titleInput.setText(wishlistItem.title)
            targetAmountInput.setText(wishlistItem.targetAmount.toInt().toString())
            monthlyContributionInput.setText(wishlistItem.monthlyContribution.toInt().toString())
            wishlistItemIconInput.setText(wishlistItem.icon)
            openWishlistTransactionHistoryPageBTN.visibility =
                if (wishlistItem.transactionDetails.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun addListeners() {
        val sharedFunctions = SharedFunctions()

        binding.apply {
            backBTN.setOnClickListener { finish() }
            sharedFunctions.updateEditTextLayoutStatus(
                contributionInput,
                contributionInputLayout
            )
            main.setOnClickListener {
                contributionInput.clearFocus()
                sharedFunctions.removeFocusFromEditTexts(
                    updateWishlistItemDetailLayout,
                    main,
                    baseContext
                )
            }
            sharedFunctions.updateEditTextLayoutStatus(titleInput, titleInputLayout)
            sharedFunctions.updateEditTextLayoutStatus(targetAmountInput, targetAmountInputLayout)
            sharedFunctions.updateEditTextLayoutStatus(
                monthlyContributionInput,
                monthlyContributionInputLayout
            )
            wishlistItemIconInput.setOnClickListener {
                wishlistItemIconInput.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE
            }
            wishlistItemIconInput.filters = arrayOf(EmojiInputFilter())
            enableUpdateCB.setOnClickListener { toggleUpdate() }
            addContributionBTN.setOnClickListener { addContribution() }
            updateWishlistItemDetailsBTN.setOnClickListener { updateWishlistItem() }
            openWishlistTransactionHistoryPageBTN.setOnClickListener {
                val intent = Intent(baseContext, WishlistItemTransactionHistoryActivity::class.java)
                intent.putExtra("transactionDetails", wishlistItem.transactionDetails)
                startActivity(intent)
            }
        }
    }

    private fun toggleUpdate() {
        binding.apply {
            titleInput.isEnabled = enableUpdateCB.isChecked
            targetAmountInput.isEnabled = enableUpdateCB.isChecked
            monthlyContributionInput.isEnabled = enableUpdateCB.isChecked
            wishlistItemIconInput.isEnabled = enableUpdateCB.isChecked
            updateWishlistItemDetailsBTN.visibility =
                if (enableUpdateCB.isChecked) View.VISIBLE else View.GONE
        }
    }

    private fun updateWishlistItem() {
        binding.apply {
            val title = titleInput.text.toString()
            val targetAmount = targetAmountInput.text.toString().toDoubleOrNull()
            val monthlyContribution =
                monthlyContributionInput.text.toString().toDoubleOrNull()
            val icon = wishlistItemIconInput.text.toString()

            if (title.isEmpty()) {
                titleInputLayout.error = "Please enter a valid title"
            } else if (targetAmount == null || targetAmount == 0.0) {
                targetAmountInputLayout.error = "Please enter a valid amount"
            } else if (monthlyContribution == null || monthlyContribution == 0.0) {
                monthlyContributionInputLayout.error = "Please enter a valid amount"
            } else {
                val wishlistItem = WishlistItem(
                    wishlistItem.id,
                    title,
                    targetAmount,
                    wishlistItem.savedAmount,
                    monthlyContribution,
                    wishlistItem.date,
                    icon,
                    wishlistItem.transactionDetails
                )
                update(wishlistItem)
            }
        }
    }

    private fun addContribution() {
        binding.apply {
            val contribution = contributionInput.text.toString().toDoubleOrNull()
            if (contribution == null || contribution == 0.0) {
                contributionInputLayout.error = "Please enter a valid amount"
            } else if ((wishlistItem.savedAmount + contribution) > wishlistItem.targetAmount) {
                contributionInputLayout.error = "Exceeds target amount"
            } else {
                val currentTime = SharedFunctions().getCurrentDateTimeInISO8601()
                val separator = if (wishlistItem.transactionDetails.isEmpty()) "" else ","
                val transactionDetail =
                    "${wishlistItem.transactionDetails}$separator$currentTime|$contribution"
                val savedAmount = wishlistItem.savedAmount + contribution
                val wishlistItem = WishlistItem(
                    wishlistItem.id,
                    wishlistItem.title,
                    wishlistItem.targetAmount,
                    savedAmount,
                    wishlistItem.monthlyContribution,
                    wishlistItem.date,
                    wishlistItem.icon,
                    transactionDetail,
                )
                update(wishlistItem)
            }
        }
    }

    private fun setOverviewData() {
        "Saving for %s".format(wishlistItem.title).also { binding.itemOverviewTitleTV.text = it }

        if (wishlistItem.targetAmount == wishlistItem.savedAmount) {
            binding.apply {
                remainingAmountTV.text = getString(R.string.wishlist_congrats_msg)
                savedPercentIndicator.setProgress(100, true)
                addContributionBTN.visibility = View.GONE
                enableUpdateCB.visibility = View.GONE
                updateWishlistItemTV.text = getString(R.string.wishlist_item)
            }
            return
        }

        val amount = "₹${wishlistItem.targetAmount - wishlistItem.savedAmount}"
        val text = "You need to save $amount to get this wishlist item."
        val spannableString = SpannableString(text)

        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            text.indexOf(amount),
            text.indexOf(amount) + amount.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val currentMonthNumber = SharedFunctions().getCurrentMonth()
        val monthlySavedAmount = wishlistItem.transactionDetails.split(",").sumOf { transaction ->
            Log.e("TEST TRANSACTION", transaction)
            if (transaction.isNotEmpty() && transaction.substring(5, 7) == currentMonthNumber) {
                transaction.split("|").last().split(".").first().toInt()
            } else {
                0
            }
        }

        if (monthlySavedAmount > wishlistItem.monthlyContribution) {
            "You've completed this month's saving goal".also {
                binding.remainingMonthlyGoalTV.text = it
            }
        } else {
            val remainingMonthlyTarget = "₹${wishlistItem.monthlyContribution - monthlySavedAmount}"
            val spannableTextTwo = "Need $remainingMonthlyTarget more to complete monthly goal"
            val spannableStringTwo = SpannableString(spannableTextTwo)

            spannableStringTwo.setSpan(
                StyleSpan(Typeface.BOLD),
                spannableTextTwo.indexOf(remainingMonthlyTarget),
                spannableTextTwo.indexOf(remainingMonthlyTarget) + remainingMonthlyTarget.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            binding.remainingMonthlyGoalTV.text = spannableStringTwo
        }

        val savedPercent = (wishlistItem.savedAmount / wishlistItem.targetAmount) * 100

        binding.apply {
            remainingAmountTV.text = spannableString
            savedPercentIndicator.setProgress(savedPercent.toInt(), true)
        }
    }

    private fun update(wishlistItem: WishlistItem) {
        val db = Room.databaseBuilder(baseContext, AppDatabase::class.java, "wishlist").build()
        lifecycleScope.launch {
            withContext(Dispatchers.IO) { db.wishlistItemDao().update(wishlistItem) }
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        setOverviewData()
    }
}