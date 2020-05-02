package com.example.bookmanager.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookmanager.R
import com.example.bookmanager.models.Book
import com.example.bookmanager.utils.Libs

class BookListAdapter(
    private val mContext: Context,
    private val mListData: MutableList<Book>,
    private val mClickListener: View.OnClickListener? = null
) : RecyclerView.Adapter<BookListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.row_book_list, parent, false)

        if (mClickListener != null) {
            view.setOnClickListener(mClickListener)
        }

        return BookListViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookListViewHolder, position: Int) {
        val item = mListData[position]
        holder.mBookTitle.text = item.mTitle
        holder.mBookAuthor.text = Libs.listToString(item.mAuthors)
        Glide.with(mContext)
            .load(item.mImage)
            .into(holder.mBookImage)
    }

    override fun getItemCount(): Int {
        return mListData.size
    }

    fun removeAll() {
        val cnt = mListData.size
        mListData.removeAll { true }
        for (i: Int in (cnt - 1)..0) {
            notifyItemRemoved(i)
        }
    }
}
