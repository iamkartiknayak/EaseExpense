package com.kartiknayak.easeexpense

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.kartiknayak.easeexpense.databinding.ActivityIntroBinding

class IntroActivity : FragmentActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: IntroAdapter
    private lateinit var binding: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layouts = listOf(
            R.layout.intro_page_one,
            R.layout.intro_page_two,
            R.layout.intro_page_three,
            R.layout.intro_page_four
        )

        viewPager = binding.viewPager
        adapter = IntroAdapter(this, layouts)
        viewPager.adapter = adapter

        setListeners(layouts)
    }

    private fun setListeners(layouts: List<Int>) {
        binding.nextLayout.setOnClickListener { updateNavigationControlLayout(layouts) }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                binding.nextTV.text = if (position == 3) "Done" else "Next"
                binding.authTile.visibility = if (position == 3) View.VISIBLE else View.GONE
            }
        })
    }

    private fun updateNavigationControlLayout(layouts: List<Int>) {
        val currentItem = viewPager.currentItem
        if (currentItem < layouts.size - 1) {
            viewPager.currentItem = currentItem + 1

            binding.nextTV.text = if (currentItem + 1 == 3) "Done" else "Next"
            return
        }

        binding.authTile.visibility = if (currentItem == 3) View.VISIBLE else View.GONE
        saveAppBootData(binding.authSwitch.isChecked)
        SharedFunctions().loadMainScreen(this)
    }

    private fun saveAppBootData(authEnabled: Boolean) {
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("IntroDone", true)
        editor.putBoolean("AuthEnabled", authEnabled)
        editor.apply()
    }

    class IntroAdapter(fragmentActivity: FragmentActivity, private val layouts: List<Int>) :
        FragmentStateAdapter(fragmentActivity) {

        override fun getItemCount(): Int = layouts.size

        override fun createFragment(position: Int): Fragment {
            return IntroFragment.newInstance(layouts[position])
        }
    }

    class IntroFragment : Fragment() {
        companion object {
            private const val ARG_LAYOUT_RES_ID = "layoutResId"

            fun newInstance(layoutResId: Int): IntroFragment {
                val fragment = IntroFragment()
                val args = Bundle()
                args.putInt(ARG_LAYOUT_RES_ID, layoutResId)
                fragment.arguments = args
                return fragment
            }
        }

        private var layoutResId: Int = 0

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            arguments?.let {
                layoutResId = it.getInt(ARG_LAYOUT_RES_ID)
            }
        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(layoutResId, container, false)
        }
    }
}
