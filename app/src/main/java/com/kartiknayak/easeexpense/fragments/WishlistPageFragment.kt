package com.kartiknayak.easeexpense.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.kartiknayak.easeexpense.AddWishlistItemActivity
import com.kartiknayak.easeexpense.SearchActivity
import com.kartiknayak.easeexpense.SharedFunctions
import com.kartiknayak.easeexpense.adapter.WishlistItemAdapter
import com.kartiknayak.easeexpense.databinding.FragmentWishlistPageBinding
import com.kartiknayak.easeexpense.db.AppDatabase
import com.kartiknayak.easeexpense.model.WishlistItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs


class WishlistPageFragment : Fragment() {
    private lateinit var binding: FragmentWishlistPageBinding
    private lateinit var progressIndicator: LinearProgressIndicator

    private lateinit var db: AppDatabase
    private lateinit var wishlist: List<WishlistItem>
    private lateinit var oldWishlist: List<WishlistItem>
    private lateinit var deletedWishlistItem: WishlistItem
    private lateinit var wishlistItemAdapter: WishlistItemAdapter

    private lateinit var sharedFunctions: SharedFunctions
    private var previousWishlistCount: Int = 0
    private var isDeleted: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentWishlistPageBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedFunctions = SharedFunctions()
        wishlistItemAdapter = WishlistItemAdapter(emptyList())
        db = Room.databaseBuilder(requireContext(), AppDatabase::class.java, "wishlist").build()

        binding.apply {
            progressIndicator = monthlyExpenseIndicator
            wishlistRecyclerView.apply {
                adapter = wishlistItemAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }

            val itemTouchHelper = sharedFunctions.getItemTouchHelper { position ->
                deleteWishlistItem(wishlist[position])
            }
            itemTouchHelper.attachToRecyclerView(wishlistRecyclerView)

            openSearchWishlistPageBTN.setOnClickListener {
                val intent = Intent(requireContext(), SearchActivity::class.java)
                intent.putExtra("searchId", 2)
                intent.putExtra("title", "Wishlist")
                startActivity(intent)
            }

            openAddToWishlistPageBTN.setOnClickListener {
                val intent = Intent(requireContext(), AddWishlistItemActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun updateUI() {
        updateWishlistDashboard()
        wishlistItemAdapter.setData(wishlist)
    }

    private fun updateWishlistDashboard() {
        val pendingWishlistCount = wishlist.count { it.targetAmount != it.savedAmount }
        val completedWishlistCount = wishlist.size - pendingWishlistCount
        val totalSavedAmount = wishlist.sumOf { it.savedAmount }
        val currentTotalSavedAmount =
            wishlist.filter { it.targetAmount != it.savedAmount }.sumOf { it.savedAmount }
        val currentTotalTargetAmount =
            wishlist.filter { it.targetAmount != it.savedAmount }.sumOf { it.targetAmount }
        val savedPercent = abs((currentTotalSavedAmount / currentTotalTargetAmount) * 100).toInt()

        if (previousWishlistCount - pendingWishlistCount > 0 && !isDeleted) {
            binding.apply {
                confettiAnimation.visibility = View.VISIBLE
                confettiAnimation.playAnimation()
            }
        }

        previousWishlistCount = pendingWishlistCount

        binding.apply {
            "₹%.2f".format(totalSavedAmount).also { totalSavedAmountTV.text = it }
            "₹%.0f".format(currentTotalTargetAmount)
                .also { currentTotalTargetAmountTV.text = it }
            "Complete : %d".format(completedWishlistCount)
                .also { completedWishlistCountTV.text = it }
            "Pending : %d".format(pendingWishlistCount)
                .also { pendingWishlistCountTV.text = it }
            "%d%%".format(savedPercent).also { savedPercentTV.text = it }

            if (wishlist.isNotEmpty()) {
                wishlistLayout.visibility = View.VISIBLE
                emptyWishlistImageLayout.visibility = View.GONE
            } else {
                wishlistLayout.visibility = View.GONE
                emptyWishlistImageLayout.visibility = View.VISIBLE
            }
        }
        progressIndicator.setProgress(savedPercent, true)
        binding.openSearchWishlistPageBTN.visibility =
            if (wishlist.size < 3) View.GONE else View.VISIBLE
    }

    private fun fetchAll() {
        lifecycleScope.launch {
            wishlist = withContext(Dispatchers.IO) { db.wishlistItemDao().fetchAll().reversed() }
            requireActivity().runOnUiThread { updateUI() }
        }
    }

    private fun undoDelete() {
        isDeleted = false
        lifecycleScope.launch {
            withContext(Dispatchers.IO) { db.wishlistItemDao().insertAll(deletedWishlistItem) }
            wishlist = oldWishlist
            requireActivity().runOnUiThread { updateUI() }
        }
    }

    private fun deleteWishlistItem(wishlistItem: WishlistItem) {
        deletedWishlistItem = wishlistItem
        oldWishlist = wishlist
        wishlist = wishlist.filter { it.id != wishlistItem.id }
        isDeleted = true

        lifecycleScope.launch {
            withContext(Dispatchers.IO) { db.wishlistItemDao().delete(wishlistItem) }
            requireActivity().runOnUiThread {
                updateUI()
                sharedFunctions.showSnackbar(
                    "Wishlist",
                    binding, requireContext()
                ) { undoDelete() }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchAll()
    }
}