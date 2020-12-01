package com.example.bookmanager.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookmanager.R
import com.example.bookmanager.databinding.ListItemBookshelfBinding
import com.example.bookmanager.rooms.entities.Book
import com.example.bookmanager.utils.FileIO
import kotlinx.coroutines.runBlocking

/**
 * 本棚ページでリスト表示するためのアダプター。
 */
class BookshelfAdapter : RecyclerView.Adapter<BookshelfAdapter.BookShelfHolder>() {

    lateinit var context: Context

    private var books: List<Book> = listOf()

    private var listener: View.OnClickListener? = null

    private var callback: Callback? = null

    interface Callback {
        fun onBindViewHolder(view: View)
    }

    fun setListener(listener: View.OnClickListener) {
        this.listener = listener
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookShelfHolder {
        context = parent.context
        val binding: ListItemBookshelfBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.list_item_bookshelf, parent, false
        )
        binding.root.setOnClickListener(listener)
        return BookShelfHolder(binding)
    }

    override fun onBindViewHolder(holder: BookShelfHolder, position: Int) {
        val book = books[position]
        val image = runBlocking { FileIO.readBookImage(context, book.id) }

        holder.binding.bookshelfItemTitle.apply {
            if (image != null) {
                background = image
                text = ""
            } else {
                background =
                    ResourcesCompat.getDrawable(context.resources, R.drawable.white_680x800, null)
                text = book.title
            }
        }

        callback?.onBindViewHolder(holder.binding.root)
    }

    override fun getItemCount(): Int {
        return books.size
    }

    fun update(books: List<Book>) {
        this.books = books
        notifyDataSetChanged()
    }

    class BookShelfHolder(val binding: ListItemBookshelfBinding) :
        RecyclerView.ViewHolder(binding.root)
}
