package com.kartiknayak.easeexpense.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kartiknayak.easeexpense.fragments.SettingsPageFragment
import com.kartiknayak.easeexpense.fragments.TransactionsPageFragment
import com.kartiknayak.easeexpense.fragments.WishlistPageFragment

class PagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TransactionsPageFragment()
            1 -> WishlistPageFragment()
            2 -> SettingsPageFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }

    override fun getItemCount(): Int = 3
}
