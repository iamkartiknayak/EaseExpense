package com.kartiknayak.easeexpense

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kartiknayak.easeexpense.adapter.PagerAdapter
import com.kartiknayak.easeexpense.databinding.ActivityBottomNavbarBinding

class BottomNavbarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBottomNavbarBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        window.statusBarColor = ContextCompat.getColor(this, R.color.accentColor)

        binding = ActivityBottomNavbarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewPager = binding.viewPager
        bottomNavigationView = binding.bottomNavigationView

        val pagerAdapter = PagerAdapter(this)
        viewPager.adapter = pagerAdapter

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.dashboardBNI -> viewPager.currentItem = 0
                R.id.wishListBNI -> viewPager.currentItem = 1
                R.id.settingsBNI -> viewPager.currentItem = 2
            }
            true
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                bottomNavigationView.menu.getItem(position).isChecked = true
            }
        })
    }
}