package com.kartiknayak.easeexpense.model

import com.kartiknayak.easeexpense.R
import java.io.Serializable

data class ExpenseCategory(
    val label: String,
    val imageId: Int,
) : Serializable

class ExpenseManager {
    private val categories = mutableListOf<ExpenseCategory>()

    fun getCategories(): List<ExpenseCategory> {
        return categories
    }

    init {
        categories.addAll(
            listOf(
                ExpenseCategory("Apparel", R.drawable.ic_apparel),
                ExpenseCategory("Bills", R.drawable.ic_bills),
                ExpenseCategory("Budget", R.drawable.ic_budget),
                ExpenseCategory("Celebration", R.drawable.ic_celebration),
                ExpenseCategory("Child Care", R.drawable.ic_child_care),
                ExpenseCategory("Donation", R.drawable.ic_donation),
                ExpenseCategory("Drink", R.drawable.ic_drink),
                ExpenseCategory("Education", R.drawable.ic_education),
                ExpenseCategory("Electronics", R.drawable.ic_electronics),
                ExpenseCategory("Entertainment", R.drawable.ic_entertainment),
                ExpenseCategory("Fees", R.drawable.ic_fees),
                ExpenseCategory("Fitness", R.drawable.ic_fitness),
                ExpenseCategory("Food", R.drawable.ic_food),
                ExpenseCategory("Fuel", R.drawable.ic_fuel),
                ExpenseCategory("Gift", R.drawable.ic_gift),
                ExpenseCategory("Grocery", R.drawable.ic_grocery),
                ExpenseCategory("Health", R.drawable.ic_health),
                ExpenseCategory("Hobby", R.drawable.ic_hobby),
                ExpenseCategory("Home", R.drawable.ic_home),
                ExpenseCategory("Hygiene", R.drawable.ic_hygine),
                ExpenseCategory("Insurance", R.drawable.ic_insurance),
                ExpenseCategory("investment", R.drawable.ic_investment),
                ExpenseCategory("Mechanic", R.drawable.ic_mechanic),
                ExpenseCategory("Medicine", R.drawable.ic_medicine),
                ExpenseCategory("Membership", R.drawable.ic_membership),
                ExpenseCategory("Miscellaneous", R.drawable.ic_miscellaneous),
                ExpenseCategory("Pet", R.drawable.ic_pet),
                ExpenseCategory("Rent", R.drawable.ic_rent),
                ExpenseCategory("Restaurant", R.drawable.ic_restaurant),
                ExpenseCategory("Self care", R.drawable.ic_self_care),
                ExpenseCategory("Shipping", R.drawable.ic_shipping),
                ExpenseCategory("Shopping", R.drawable.ic_shopping),
                ExpenseCategory("Subscription", R.drawable.ic_subscription),
                ExpenseCategory("Tax", R.drawable.ic_tax),
                ExpenseCategory("Transportation", R.drawable.ic_transportation),
                ExpenseCategory("Utility", R.drawable.ic_utility),
                ExpenseCategory("Vacation", R.drawable.ic_vacation),
            )
        )
    }
}