package com.kartiknayak.easeexpense.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.kartiknayak.easeexpense.R
import com.kartiknayak.easeexpense.databinding.LayoutCategoryCardBinding
import com.kartiknayak.easeexpense.model.ExpenseCategory


class CategoryAdapter(
    private var categories: List<ExpenseCategory>,
) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(parent?.context)
            .inflate(R.layout.layout_category_card, parent, false)

        val holder: ViewHolder = (view.tag as? ViewHolder) ?: ViewHolder(view).apply {
            view.tag = this
        }

        val category = categories[position]
        holder.binding.apply {
            categoryImageIV.setImageResource(category.imageId)
            categoryNameTV.text = category.label
        }
        return view
    }

    private class ViewHolder(view: View) {
        val binding: LayoutCategoryCardBinding = LayoutCategoryCardBinding.bind(view)
    }

    override fun getItem(position: Int): Any = categories[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = categories.size
}