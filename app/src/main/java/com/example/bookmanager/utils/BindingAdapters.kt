package com.example.bookmanager.utils

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookmanager.models.Book
import com.example.bookmanager.views.BookListAdapter

object BindingAdapters {
    @JvmStatic
    @BindingAdapter("resultBooks")
    fun RecyclerView.bindResultBooks(resultBooks: List<Book>?) {
        resultBooks ?: return
        (adapter as BookListAdapter).update(resultBooks)
    }
}
