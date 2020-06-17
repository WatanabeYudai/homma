package com.example.bookmanager.views

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookmanager.R
import com.example.bookmanager.databinding.ListItemBookSearchBinding
import com.example.bookmanager.models.Book
import com.example.bookmanager.utils.Libs

class BookSearchAdapter(
    private val activity: Activity,
    private var resultBooks: List<Book>,
    private val clickListener: View.OnClickListener? = null
) : RecyclerView.Adapter<BookSearchAdapter.BookSearchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookSearchViewHolder {
        val binding: ListItemBookSearchBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.list_item_book_search,
            parent,
            false
        )
        binding.root.setOnClickListener(clickListener)
        return BookSearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookSearchViewHolder, position: Int) {
        val resultBook = resultBooks[position]
        holder.binding.apply {
            lifecycleOwner = activity as LifecycleOwner
            titleBookSearchItem.text = resultBook.title
            authorBookSearchItem.text = Libs.listToString(resultBook.authors)
        }
        Glide.with(activity)
            .load(resultBook.image)
            .into(holder.binding.imageBookSearchItem)
    }

    override fun getItemCount(): Int {
        return resultBooks.size
    }

    fun update(resultBooks: List<Book>) {
        this.resultBooks = resultBooks
        notifyDataSetChanged()
    }

    inner class BookSearchViewHolder(val binding: ListItemBookSearchBinding) :
        RecyclerView.ViewHolder(binding.root)
}
