package com.example.bookmanager.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookmanager.R
import com.example.bookmanager.databinding.ListItemBookSearchBinding
import com.example.bookmanager.models.BookSearchResult
import com.example.bookmanager.utils.Libs

class BookSearchAdapter : RecyclerView.Adapter<BookSearchAdapter.BookSearchViewHolder>() {

    private lateinit var context: Context
    private var resultBooks: List<BookSearchResult> = listOf()
    private var listener: View.OnClickListener? = null

    fun setListener(listener: View.OnClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookSearchViewHolder {
        context = parent.context
        val binding: ListItemBookSearchBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.list_item_book_search,
            parent,
            false
        )
        binding.root.setOnClickListener(listener)
        return BookSearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookSearchViewHolder, position: Int) {
        val resultBook = resultBooks[position]
        holder.binding.apply {
            lifecycleOwner = context as LifecycleOwner
            bookSearchItemTitle.text = resultBook.title
            bookSearchItemAuthor.text = Libs.listToString(resultBook.authors)
        }
        Glide.with(context)
            .load(resultBook.image)
            .into(holder.binding.bookSearchItemImage)
    }

    override fun getItemCount(): Int {
        return resultBooks.size
    }

    fun update(resultBooks: List<BookSearchResult>) {
        this.resultBooks = resultBooks
        notifyDataSetChanged()
    }

    // TODO: 本棚に登録 -> DB に保存 -> Glide を使って Bitmap 形式で画像を取得 -> 内部ストレージに保存

    inner class BookSearchViewHolder(val binding: ListItemBookSearchBinding) :
        RecyclerView.ViewHolder(binding.root)
}
