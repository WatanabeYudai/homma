package com.example.bookmanager.views

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bookmanager.R
import com.example.bookmanager.databinding.ActivityMainBinding
import com.example.bookmanager.viewmodels.BookshelfViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BookshelfActivity : AppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(this.application)
        ).get(BookshelfViewModel::class.java)
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.also {
            it.viewModel = viewModel
            it.lifecycleOwner = this
        }

        initToolbar()
        initRecyclerView()
        setFabClickListener()
    }

    override fun onStart() {
        super.onStart()

        GlobalScope.launch { viewModel.reload() }
    }

    private fun initRecyclerView() {
        val adapter = BookshelfAdapter()
        val manager = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
        binding.bookshelfRoot.also {
            it.layoutManager = manager
            it.adapter = adapter
//            it.addItemDecoration(DividerItemDecoration(this, manager.orientation))
        }
    }

    private fun setFabClickListener() {
        binding.fabAddBook.setOnClickListener {
            val intent = Intent(applicationContext, BookSearchActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar).apply {
            setTitle(R.string.toolbar_title)
            setTitleTextColor(Color.WHITE)
        }
        setSupportActionBar(toolbar)
    }
}
