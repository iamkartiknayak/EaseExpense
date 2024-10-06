package com.kartiknayak.easeexpense

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.kartiknayak.easeexpense.databinding.ActivityAddWishlistItemBinding
import com.kartiknayak.easeexpense.db.AppDatabase
import com.kartiknayak.easeexpense.model.WishlistItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddWishlistItemActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddWishlistItemBinding

    private var sharedFunctions = SharedFunctions()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddWishlistItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
    }

    private fun setListeners() {
        binding.apply {
            main.setOnClickListener {
                sharedFunctions.removeFocusFromEditTexts(
                    addWishlistItemEditTextControlsLayout,
                    main,
                    baseContext
                )
            }
            wishlistItemIconInput.filters = arrayOf(EmojiInputFilter())
            sharedFunctions.updateEditTextLayoutStatus(titleInput, titleInputLayout)
            sharedFunctions.updateEditTextLayoutStatus(targetAmountInput, targetAmountInputLayout)
            sharedFunctions.updateEditTextLayoutStatus(
                monthlyContributionInput,
                monthlyContributionInputLayout
            )
            backBTN.setOnClickListener { finish() }
            addToWishlistItemBTN.setOnClickListener { addWishlistItem() }
        }
    }

    private fun addWishlistItem() {
        binding.apply {
            val title = titleInput.text.toString()
            val targetAmount = targetAmountInput.text.toString().toDoubleOrNull()
            val monthlyContribution = monthlyContributionInput.text.toString().toDoubleOrNull()
            val formattedDate = sharedFunctions.getCurrentDateTimeInISO8601()
            val icon = wishlistItemIconInput.text.toString()

            if (title.isEmpty()) {
                titleInputLayout.error = "Please enter a valid title"
            } else if (targetAmount == null || targetAmount == 0.0) {
                targetAmountInputLayout.error = "Please enter a valid amount"
            } else if (monthlyContribution == null || monthlyContribution == 0.0) {
                monthlyContributionInputLayout.error = "Please enter a valid amount"
            } else if (monthlyContribution > targetAmount) {
                monthlyContributionInputLayout.error =
                    "Monthly contribution cannot be greater than target amount"
            } else {
                val wishlistItem = WishlistItem(
                    0,
                    title,
                    targetAmount,
                    0.0,
                    monthlyContribution,
                    formattedDate,
                    icon,
                    ""
                )
                insert(wishlistItem)
            }
        }
    }

    private fun insert(wishlistItem: WishlistItem) {
        val db = Room.databaseBuilder(baseContext, AppDatabase::class.java, "wishlist").build()
        lifecycleScope.launch {
            withContext(Dispatchers.IO) { db.wishlistItemDao().insertAll(wishlistItem) }
            finish()
        }
    }
}