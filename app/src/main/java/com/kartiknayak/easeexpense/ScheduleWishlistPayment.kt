//package com.kartiknayak.easeexpense
//
//import android.app.Activity
//import android.app.Fragment
//import androidx.lifecycle.LifecycleCoroutineScope
//import androidx.room.Room
//import com.kartiknayak.easeexpense.db.AppDatabase
//import com.kartiknayak.easeexpense.model.WishlistItem
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//
//class ScheduleWishlistPayment : Fragment() {
//    private lateinit var db: AppDatabase
//    private lateinit var wishlist: List<WishlistItem>
//    private lateinit var privateLifecycleScope: LifecycleCoroutineScope
//
//    private fun checkForTodaySchedule() {
//        val currentDateTime = SharedFunctions().getCurrentDateTimeInISO8601()
//        val currentDate = currentDateTime.substring(8, 10).toInt()
//        val currentMonth = currentDateTime.substring(5, 7)
//        var count = 0
//        wishlist = wishlist.map { item ->
////            if (!(item.transactionDetails.split(",").last()
////                    .contains("auto") && item.transactionDetails.split(",").last()
////                    .contains(currentMonth)) && item.savedAmount != item.targetAmount
////            ) {
//
//            if (!(item.transactionDetails.split(",").last()
//                    .contains("auto") && item.transactionDetails.split(",").last()
//                    .contains(currentMonth)) && item.savedAmount != item.targetAmount && item.date.substring(8, 10).toInt() >= 28 && currentDate >=
//            ) {
//                count += 1
//                if (item.savedAmount + item.monthlyContribution > item.targetAmount) {
//                    val amountToDebit = item.targetAmount - item.savedAmount
//                    item.copy(
//                        savedAmount = item.savedAmount + amountToDebit,
//                        transactionDetails = item.transactionDetails + ",$currentDateTime|$amountToDebit|auto"
//                    )
//                } else {
//                    val amountToDebit = item.savedAmount + item.monthlyContribution
//                    item.copy(
//                        savedAmount = amountToDebit,
//                        transactionDetails = item.transactionDetails + ",$currentDateTime|$amountToDebit|auto"
//                    )
//                }
//            } else {
//                item
//            }
//        }
//
//        if (count == 0) return
//        wishlist.forEach { item ->
//            privateLifecycleScope.launch {
//                withContext(Dispatchers.IO) { db.wishlistItemDao().update(item) }
//            }
//        }
//    }
//
//    fun performAutoPayment(lifecycleScope: LifecycleCoroutineScope, activity: Activity) {
//        db = Room.databaseBuilder(activity, AppDatabase::class.java, "wishlist").build()
//        privateLifecycleScope = lifecycleScope
//        privateLifecycleScope.launch {
//            wishlist = withContext(Dispatchers.IO) {
//                db.wishlistItemDao().fetchAll().reversed()
//            }
//            activity.runOnUiThread { checkForTodaySchedule() }
//        }
//    }
//}