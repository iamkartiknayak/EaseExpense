package com.kartiknayak.easeexpense

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

class SharedFunctions {
    // returns date param with appropriate suffix concatenated as string
    private fun getDateWithSuffix(day: Int): String {
        return when {
            day in 11..13 -> "${day}th"
            day % 10 == 1 -> "${day}st"
            day % 10 == 2 -> "${day}nd"
            day % 10 == 3 -> "${day}rd"
            else -> "${day}th"
        }
    }

    // returns final date format (23rd May 2024) to be used for displaying data in tile adapter
    private fun formatWithDateSuffix(date: LocalDate): String {
        val day = date.dayOfMonth
        val dateSuffix = getDateWithSuffix(day)
        return "$dateSuffix ${date.month} ${date.year}"
    }

    // date-time picker to select current date and time in ISO8601 format
    fun showDatePickerDialog(
        context: Context,
        dateInput: EditText,
        callback: (String) -> Unit,
    ) {
        val today = LocalDate.now()
        val listener =
            DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDay ->
                val date = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)

                val currentTime = LocalTime.now()
                val dateTime = LocalDateTime.of(date, currentTime)
                val zonedDateTime = dateTime.atZone(ZoneId.of("UTC"))
                val isoDate = zonedDateTime.format(
                    DateTimeFormatter.ofPattern(
                        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                        Locale.getDefault()
                    )
                )
                val formattedDate = formatWithDateSuffix(date)
                dateInput.setText(formattedDate)
                callback(isoDate)
            }

        DatePickerDialog(
            context,
            R.style.DatePickerTheme,
            listener,
            today.year,
            today.monthValue - 1,
            today.dayOfMonth
        ).apply {
            datePicker.maxDate =
                today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }.show()
    }

    // convert ISO8601 date format to human readable format
    fun convertDateToReadableFormat(iso8601String: String): String {
        val dateTime = ZonedDateTime.parse(iso8601String, DateTimeFormatter.ISO_ZONED_DATE_TIME)
        val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
        val formattedDate = dateTime.format(formatter)
        val day = dateTime.dayOfMonth
        val dateSuffix = getDateWithSuffix(day)

        return formattedDate.replaceFirst(Regex("""\d+"""), dateSuffix)
    }

    // fetch current data-time in ISO8601 format
    fun getCurrentDateTimeInISO8601(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        return currentDateTime.format(formatter)
    }

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // removes focus from all editTexts in an activity
    fun removeFocusFromEditTexts(linearLayout: LinearLayout, rootView: View, context: Context) {
        linearLayout.let { layout ->
            for (i in 0 until layout.childCount) {
                val child = layout.getChildAt(i)
                if (child is TextInputLayout) {
                    val editText = child.editText
                    editText?.clearFocus()
                }
            }
        }

        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(rootView.windowToken, 0)
    }

    // removes TextInputLayout error status when EditText text content is changed
    fun updateEditTextLayoutStatus(
        editText: EditText,
        textInputLayout: TextInputLayout,
        callback: (() -> Unit)? = null
    ) {
        editText.addTextChangedListener {
            if (it!!.isNotEmpty()) {
                textInputLayout.error = null
            }
            callback?.invoke()
        }
    }

    // used to remove element from recyclerview
    fun getItemTouchHelper(callback: (Int) -> Unit): ItemTouchHelper {
        val itemTouchHelperCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder,
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.bindingAdapterPosition
                    callback(position)
                }
            }
        return ItemTouchHelper(itemTouchHelperCallback)
    }

    fun showSnackbar(type: String, binding: ViewBinding, context: Context, callback: () -> Unit) {
        val snackbar = Snackbar.make(binding.root, "$type deleted", Snackbar.LENGTH_LONG)
        snackbar.setAction("Undo") { callback() }
        snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.white))
        snackbar.setTextColor(ContextCompat.getColor(context, R.color.white))
        snackbar.setBackgroundTint(ContextCompat.getColor(context, R.color.accentColor))
        snackbar.show()
    }

    // compress image-byte array for efficient space usage
    private fun compressByteArray(byteArray: ByteArray): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, outputStream)
        return outputStream.toByteArray()
    }

    // converts the image data to byte-array
    private fun imageUriToByteArray(context: Context, imageUri: Uri): ByteArray {
        val byteArray = context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
            inputStream.readBytes()
        }!!
        return compressByteArray(byteArray)
    }

    // opens image picker to add image to transaction
    fun getImagePicker(
        activity: ComponentActivity,
        imageByteArrayCallback: (ByteArray) -> Unit,
        bindDataCallback: () -> Unit,
    ): ActivityResultLauncher<PickVisualMediaRequest> {
        return activity.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                try {
                    val byteArray = imageUriToByteArray(activity.baseContext, it)
                    imageByteArrayCallback(byteArray)
                } catch (e: Exception) {
                    Log.e("IMAGE_PICKER_ERROR", e.message.toString())
                }
                bindDataCallback()
            }
        }
    }

    // opens image if image-byte data exists else opens image picker
    fun pickOrViewImage(
        imageByteArray: ByteArray?,
        context: Context,
        imagePicker: ActivityResultLauncher<PickVisualMediaRequest>,
    ) {
        if (imageByteArray != null) {
            val tempFile = File(context.cacheDir, "temp_image.jpg").apply {
                FileOutputStream(this).use { it.write(imageByteArray) }
            }

            val tempUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )

            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(tempUri, "image/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(this)
            }
        } else {
            imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    // loads the main screen of the app with which user interacts
    fun loadMainScreen(context: Context) {
        val intent = Intent(context, BottomNavbarActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }

    // fetch the shared-preference data
    fun getAppBootData(context: Context): Pair<Boolean, Boolean> {
        val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val introDone = sharedPreferences.getBoolean("IntroDone", false)
        val authEnabled = sharedPreferences.getBoolean("AuthEnabled", false)
        return Pair(introDone, authEnabled)
    }

    // fetch current month number
    fun getCurrentMonth(): String {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1
        return String.format(Locale.getDefault(), "%02d", month)
    }

    fun getMonthName(monthNumber: Int): String {
        return Month.of(monthNumber).getDisplayName(TextStyle.FULL, Locale.getDefault())
    }

    //  formats time from ISO8601 to 12Hr format
    fun getFormattedTime(isoString: String): String {
        val zonedDateTime = ZonedDateTime.parse(isoString, DateTimeFormatter.ISO_ZONED_DATE_TIME)
        val outputFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
        return zonedDateTime.format(outputFormatter)
    }
}