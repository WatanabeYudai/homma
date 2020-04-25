package com.example.bookmanager

import android.os.Bundle
import android.os.Handler
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookmanager.searchresoult.SearchResult
import com.google.android.material.snackbar.Snackbar
import com.mancj.materialsearchbar.MaterialSearchBar
import com.squareup.moshi.Moshi
import okhttp3.*
import java.io.IOException

class BookSearchActivity : AppCompatActivity() {

    var handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_search)

        initSearchBar()
        initSpinner()
        createDummyResults()
    }

    private fun initSearchBar() {
        val searchBar = findViewById<MaterialSearchBar>(R.id.book_search_bar)
        searchBar.setOnSearchActionListener(SearchActionListener())
        searchBar.openSearch()
    }

    private fun initSpinner() {
        val spinner = findViewById<Spinner>(R.id.spinner_book_search_type)
        val items = listOf(Const.SEARCH_FREE_WORD, Const.SEARCH_TITLE, Const.SEARCH_AUTHOR)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun createDummyResults() {
        val recyclerView = findViewById<RecyclerView>(R.id.book_search_results)
        val adapter = BookSearchResultsAdapter(createDummyData())
        val manager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = manager

        val decorator = DividerItemDecoration(this, manager.orientation)
        recyclerView.addItemDecoration(decorator)
    }

    private fun createDummyData(): MutableList<NewBook> {
        val newBooks = mutableListOf<NewBook>()
        for (i in 1..10) {
            newBooks.add(
                NewBook(
                    "進撃の巨人${i}",
                    listOf("諫山創"),
                    "巨人たちが襲いかかる！",
                    "image.jpg"
                )
            )
        }
        return newBooks
    }

    private inner class SearchActionListener : MaterialSearchBar.OnSearchActionListener {
        override fun onButtonClicked(buttonCode: Int) {}

        override fun onSearchStateChanged(enabled: Boolean) {}

        override fun onSearchConfirmed(text: CharSequence?) {
            // 現在表示されているリストをクリア。
            removeAllItem()
            // 入力値を取得してパラメーター付き URL を作成。
            val searchBar = findViewById<MaterialSearchBar>(R.id.book_search_bar)
            val spinner = findViewById<Spinner>(R.id.spinner_book_search_type)
            val keyword = searchBar.text
            val searchType = spinner.selectedItem.toString()
            val param = createUrlParameter(searchType, keyword)
            val url = Const.BOOK_SEARCH_API_URL + param

            val req = Request.Builder().url(url).get().build()
            val call = OkHttpClient().newCall(req)
            call.enqueue(HttpCallback())
        }
    }

    private fun removeAllItem() {
        val recyclerView = findViewById<RecyclerView>(R.id.book_search_results)
        val adapter = recyclerView.adapter as BookSearchResultsAdapter
        val cnt = adapter.itemCount
        adapter.removeAll()
        for (i in (cnt - 1)..0) {
            adapter.notifyItemRemoved(i)
        }
    }

    private fun createUrlParameter(type: String, keyword: String, index: Int = 0): String {
        var param = "?q="
        param = when (type) {
            Const.SEARCH_TITLE -> param + Const.PARAM_TITLE + ":"
            Const.SEARCH_AUTHOR -> param + Const.PARAM_AUTHOR + ":"
            else -> param
        }
        return param + keyword
    }

    inner class HttpCallback : Callback {
        override fun onFailure(call: Call, e: IOException) {}

        override fun onResponse(call: Call, response: Response) {
            val resBody = response.body?.string()
            val adapter = Moshi.Builder().build().adapter(SearchResult::class.java)
            if (resBody.isNullOrBlank()) {
                showSnackbar(getString(R.string.search_error))
                return
            }

            val result = adapter.fromJson(resBody)
            if (result == null) {
                showSnackbar(getString(R.string.search_error))
                return
            }

            handler.post { createSearchResultView(result) }
//            createSearchResultView(result)
        }
    }

    private fun createSearchResultView(result: SearchResult) {
        // 先頭から 10 個分の NewBook オブジェクトを生成
        // 10 行分のリストを生成
        // スクロールが一番下に到達したら、追加で 10 行分を生成
        val items = result.items
        if (items.isNullOrEmpty()) {
            showSnackbar(getString(R.string.search_no_item))
            return
        }

        val newBooks = mutableListOf<NewBook>()
        items.forEach { item ->
            val info = item.volumeInfo
            val title = if (info?.title != null) info.title as String else ""
            val authors = if (info?.authors != null) info.authors as List<String> else ArrayList()
            val desc = if (info?.description != null) info.description as String else ""
            val image = if (info?.imageLinks?.thumbnail != null) {
                info.imageLinks?.thumbnail as String
            } else {
                ""
            }
            newBooks.add(NewBook(title, authors, desc, image))
        }

        val recyclerView = findViewById<RecyclerView>(R.id.book_search_results)
        val adapter = BookSearchResultsAdapter(newBooks)
        recyclerView.adapter = adapter


//        val decorator = DividerItemDecoration(this, manager.orientation)
//        recyclerView.addItemDecoration(decorator)
    }

    private fun showSnackbar(msg: String) {
        Snackbar.make(
            findViewById(R.id.book_search_root),
            msg,
            Snackbar.LENGTH_LONG
        ).show()
    }
}
