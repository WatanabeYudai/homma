package com.example.bookmanager.views

import android.animation.ObjectAnimator
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bookmanager.R
import com.example.bookmanager.databinding.ActivityBookshelfBinding
import com.example.bookmanager.models.BookSortCondition
import com.example.bookmanager.rooms.common.BookRepository
import com.example.bookmanager.rooms.entities.Book
import com.example.bookmanager.utils.C
import com.example.bookmanager.utils.FileIO
import com.example.bookmanager.viewmodels.BookshelfViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


/**
 * 本棚ページのアクティビティ。
 */
class BookshelfActivity : AppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProvider(
            this, ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(BookshelfViewModel::class.java)
    }

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityBookshelfBinding>(this, R.layout.activity_bookshelf)
    }

    private val bookRepository by lazy { BookRepository(this) }

    private var selectedBook: View? = null

    private lateinit var selectedFilterButton: Button

    private lateinit var selectedSortButton: RadioButton

    private var sortViewIsShown = false

    private var selectedFilter: Book.Status? = null

    private var selectedSort: BookSortCondition = BookSortCondition(Book.Column.CREATED_AT, false)

    companion object {
        const val MENU_DETAIL = 0
        const val MENU_DELETE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.also {
            it.viewModel = viewModel
            it.lifecycleOwner = this
        }

        selectedFilterButton = binding.filterButtons.filterButtonAll
        selectedSortButton = binding.sortView.sortViewAddedAtDescRadioButton
        binding.sortView.sortViewAddedAtDescRadioButton.isChecked = true

        initToolbar()
        initRecyclerView()
        setFabClickListener()
        setFilterButtonsClickListener()
        setSortButtonsClickListener()

        binding.sortView.sortViewCloseButton.setOnClickListener {
            closeSortView()
        }
        binding.surfaceView.setOnClickListener {
            closeSortView()
        }
    }

    override fun onStart() {
        super.onStart()

        showBooksAccordingToSelectedFilter()
    }

    private fun initToolbar() {
        val toolbar: Toolbar = binding.toolbar.apply {
            setTitle(R.string.toolbar_title_bookshelf)
        } as Toolbar
        setSupportActionBar(toolbar)
    }

    private fun initRecyclerView() {
        val listener = View.OnClickListener {
            // クリックされた ImageView / TextView の２つ親の FrameLayout を渡す必要がある。
            val rootView = it.parent.parent as View
            val position = binding.bookshelfBookList.getChildAdapterPosition(rootView)
            startBookDetailActivity(position)
        }

        val adapter = BookshelfAdapter().apply {
            setOnClickListener(listener)
            setOnBindViewHolderListener(object : BookshelfAdapter.OnBindViewHolderListener {
                override fun onBound(view: View) {
                    registerForContextMenu(view)
                }
            })
        }

        val spanCount = resources.getInteger(R.integer.bookshelf_grid_span_count)
        val manager = GridLayoutManager(this, spanCount, GridLayoutManager.VERTICAL, false)

        binding.bookshelfBookList.also {
            it.layoutManager = manager
            it.adapter = adapter
            it.setHasFixedSize(true)
        }
    }

    private fun setFabClickListener() {
        binding.fabAddBook.setOnClickListener {
            val intent = Intent(applicationContext, BookSearchActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setFilterButtonsClickListener() {
        val buttons = binding.filterButtons.run {
            listOf(
                filterButtonAll,
                filterButtonWantToRead,
                filterButtonReading,
                filterButtonFinished
            )
        }
        buttons.forEach { button ->
            button.setOnClickListener {
                val clickedButton = it as Button
                switchButtonBackground(clickedButton)
                updateSelectedFilter(clickedButton)
                showBooksAccordingToSelectedFilter()
            }
        }
    }

    private fun switchButtonBackground(clickedButton: Button) {
        if (clickedButton.id == selectedFilterButton.id) {
            return
        }

        val selectedBackground = ContextCompat.getDrawable(this, R.drawable.bg_filter_btn_selected)
        val selectableBackground = ContextCompat.getDrawable(this, R.drawable.bg_filter_btn_selectable)
        selectedFilterButton.background = selectableBackground
        selectedFilterButton = clickedButton
        clickedButton.background = selectedBackground
    }

    private fun updateSelectedFilter(filterButton: Button) {
        selectedFilter = when (filterButton.id) {
            R.id.filter_button_all -> null
            R.id.filter_button_want_to_read -> Book.Status.WANT_TO_READ
            R.id.filter_button_reading -> Book.Status.READING
            R.id.filter_button_finished -> Book.Status.FINISHED
            else -> null
        }
    }

    private fun showBooksAccordingToSelectedFilter() {
        val books = runBlocking { viewModel.fetchBooks(selectedFilter, selectedSort) }
        if (books.isNullOrEmpty()) {
            binding.noBookText.text = getNoBoosText()
            binding.noBookText.visibility = View.VISIBLE
        } else {
            binding.noBookText.text = ""
            binding.noBookText.visibility = View.GONE
        }
    }

    private fun getNoBoosText(): String {
        return when (selectedFilter) {
            Book.Status.WANT_TO_READ -> getString(R.string.no_books_want_to_read_on_bookshelf)
            Book.Status.READING -> getString(R.string.no_books_reading_on_bookshelf)
            Book.Status.FINISHED -> getString(R.string.no_books_finished_on_bookshelf)
            else -> getString(R.string.no_books_on_bookshelf)
        }
    }

    private fun startBookDetailActivity(position: Int) {
        val book = viewModel.books.value?.get(position) ?: return
        startActivity(Intent(applicationContext, BookDetailActivity::class.java).apply {
            putExtra(C.BOOK_ID, book.id)
        })
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?, view: View?, menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        menu?.let {
            it.add(Menu.NONE, 0, Menu.NONE, getString(R.string.detail))
            it.add(Menu.NONE, 1, Menu.NONE, getString(R.string.delete))
        }

        view?.let {
            selectedBook = it.parent.parent as View
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val position = selectedBook?.let {
            binding.bookshelfBookList.getChildAdapterPosition(it)
        }
        position ?: return super.onContextItemSelected(item)

        when (item.itemId) {
            MENU_DETAIL -> {
                selectedBook?.let { startBookDetailActivity(position) }
            }
            MENU_DELETE -> {
                val book = viewModel.books.value?.get(position) ?: return super.onContextItemSelected(item)
                val dialog = createDeleteConfirmationDialog(book)
                dialog.show(supportFragmentManager, C.DIALOG_TAG_DELETE_BOOK)
            }
        }
        return super.onContextItemSelected(item)
    }

    private fun createDeleteConfirmationDialog(book: Book): SimpleDialogFragment {
        return SimpleDialogFragment().also {
            it.setTitle(book.title)
            it.setMessage(getString(R.string.delete_dialog_message))
            it.setPositiveButton(getString(R.string.yes), DialogInterface.OnClickListener { _, _ ->
                runBlocking {
                    bookRepository.deleteBook(book)
                    FileIO.deleteBookImage(this@BookshelfActivity, book.id)
                    showBooksAccordingToSelectedFilter()
                }
            })
            it.setNegativeButton(getString(R.string.cancel), null)
        }
    }

    private fun showSortViewWithSlideAnim() {
        val height = getSortViewHeight().toFloat()
        ObjectAnimator.ofFloat(binding.bookshelfSortView, "translationY", height * -1).apply {
            duration = 300
            start()
        }
    }

    private fun hideSortViewWithSlideAnim() {
        val height = getSortViewHeight().toFloat()
        ObjectAnimator.ofFloat(binding.bookshelfSortView, "translationY", height).apply {
            duration = 300
            start()
        }
    }

    private fun getSortViewHeight(): Int {
        return binding.bookshelfSortView.height
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.bookshelf_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.toolbar_sort -> {
                binding.surfaceView.isClickable = true
                sortViewIsShown = true
                showSortViewWithSlideAnim()
                fadeInSurfaceView()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun closeSortView() {
        binding.surfaceView.isClickable = false
        sortViewIsShown = false
        hideSortViewWithSlideAnim()
        fadeOutSurfaceView()
    }

    private fun fadeInSurfaceView() {
        binding.surfaceView.apply {
            visibility = View.VISIBLE
            startAnimation(AlphaAnimation(0.0F, 0.7F).apply {
                duration = 300
                fillAfter = true
            })
        }
    }

    private fun fadeOutSurfaceView() {
        binding.surfaceView.apply {
            startAnimation(AlphaAnimation(0.7F, 0.0F).apply {
                duration = 300
                fillAfter = true
            })
            visibility = View.GONE
        }
    }

    private fun setSortButtonsClickListener() {
        getSortButtons().forEach { button ->
            button.setOnCheckedChangeListener { compoundButton, _ ->
                if (compoundButton.id == selectedSortButton.id) {
                    return@setOnCheckedChangeListener
                }
                selectedSortButton.isChecked = false
                selectedSortButton = compoundButton as RadioButton
                sortBooks(selectedSortButton.id)
                Handler().postDelayed({
                    closeSortView()
                }, 500)
            }
        }
    }

    private fun sortBooks(radioButtonId: Int) {
        when (radioButtonId) {
            binding.sortView.sortViewTitleAscRadioButton.id -> {
                selectedSort.column = Book.Column.TITLE
                selectedSort.isAsc = true
            }
            binding.sortView.sortViewTitleDescRadioButton.id -> {
                selectedSort.column = Book.Column.TITLE
                selectedSort.isAsc = false
            }
            binding.sortView.sortViewAuthorAscRadioButton.id -> {
                selectedSort.column = Book.Column.AUTHOR
                selectedSort.isAsc = true
            }
            binding.sortView.sortViewAuthorDescRadioButton.id -> {
                selectedSort.column = Book.Column.AUTHOR
                selectedSort.isAsc = false
            }
            binding.sortView.sortViewAddedAtAscRadioButton.id -> {
                selectedSort.column = Book.Column.CREATED_AT
                selectedSort.isAsc = true
            }
            binding.sortView.sortViewAddedAtDescRadioButton.id -> {
                selectedSort.column = Book.Column.CREATED_AT
                selectedSort.isAsc = false
            }
        }

        GlobalScope.launch { viewModel.fetchBooks(selectedFilter, selectedSort) }
    }

    private fun getSortButtons(): List<RadioButton> {
        return listOf(
            binding.sortView.sortViewTitleAscRadioButton,
            binding.sortView.sortViewTitleDescRadioButton,
            binding.sortView.sortViewAuthorAscRadioButton,
            binding.sortView.sortViewAuthorDescRadioButton,
            binding.sortView.sortViewAddedAtAscRadioButton,
            binding.sortView.sortViewAddedAtDescRadioButton
        )
    }

    override fun onBackPressed() {
        if (sortViewIsShown) {
            closeSortView()
        } else {
            super.onBackPressed()
        }
    }
}
