package com.example.bookmanager.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import com.example.bookmanager.R
import com.example.bookmanager.models.BookSortCondition
import com.example.bookmanager.rooms.database.BookDatabase
import com.example.bookmanager.rooms.entities.Book
import com.example.bookmanager.utils.C
import kotlinx.coroutines.runBlocking
import java.util.*

/**
 * 本棚に保存されている本の情報を保持するための ViewModel。
 */
class BookshelfViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val bookDao = Room.databaseBuilder(
        context, BookDatabase::class.java, C.DB_NAME
    ).build().bookDao()

    private val _books: MutableLiveData<List<Book>> = MutableLiveData()

    val books: LiveData<List<Book>> = _books

    suspend fun fetchBooks(status: Book.Status?, condition: BookSortCondition): List<Book> {
        val books = if (status == null) {
            bookDao.loadAll()
        } else {
            bookDao.loadBooksByStatus(status.code)
        }
        val sortedBooks = getSortedBooks(books, condition)
        _books.postValue(sortedBooks)
        return books
    }

    private fun getSortedBooks(books: List<Book>, condition: BookSortCondition): List<Book> {
        return when (condition.column) {
            Book.Column.TITLE -> sortByTitle(books, condition.isAsc)
            Book.Column.AUTHOR -> sortByAuthor(books, condition.isAsc)
            Book.Column.CREATED_AT -> sortByDateAdded(books, condition.isAsc)
            Book.Column.RATING -> sortByRating(books, condition.isAsc)
        }
    }

    private fun sortByTitle(books: List<Book>, isAsc: Boolean): List<Book> {
        val groupedBooksList = books.groupBy { it.seriesName }
        val seriesNames = groupedBooksList.keys
        val sortedSeriesNames = if (isAsc) {
            seriesNames.sorted()
        } else {
            seriesNames.sortedDescending()
        }
        var sortedBooks = listOf<Book>()
        sortedSeriesNames.forEach { series ->
            val seriesBooks = groupedBooksList[series] ?: return@forEach
            sortedBooks = sortedBooks + sortByPublishedDate(seriesBooks, isAsc)
        }

        return sortedBooks
    }

    private fun sortByAuthor(books: List<Book>, isAsc: Boolean): List<Book> {
        val noAuthorString = context.getString(R.string.hyphen)
        val ids = books.map { it.id }
        val bookInfoList = runBlocking { bookDao.loadBookInfosByIds(ids) }
        val infoListHaveAuthor = bookInfoList.filter { it.authors.first().name != noAuthorString }
        val infoListHaveNoAuthor = bookInfoList.filter { it.authors.first().name == noAuthorString }
        val groupedBookInfoList = infoListHaveAuthor.groupBy { it.authors.first().name }.let {
            if (isAsc) {
                it.toSortedMap()
            } else {
                it.toSortedMap(Comparator.reverseOrder())
            }
        }
        var sortedBooks = listOf<Book>()
        groupedBookInfoList.forEach { (_, infoList) ->
            sortedBooks = sortedBooks + sortByTitle(infoList.map { it.book }, true)
        }

        return sortedBooks + sortByTitle(infoListHaveNoAuthor.map { it.book }, true)
    }

    private fun sortByDateAdded(books: List<Book>, newToOld: Boolean): List<Book> {
        return if (newToOld) {
            books.sortedByDescending { it.createdAt }
        } else {
            books.sortedBy { it.createdAt }
        }
    }

    private fun sortByRating(books: List<Book>, isAsc: Boolean): List<Book> {
        val booksHaveNoRating = books.filter { it.rating == 0 }
        val booksHaveRating = books.filter { it.rating > 0 }
        val groupedBooks = booksHaveRating.groupBy { it.rating }.let {
            if (isAsc) {
                it.toSortedMap()
            } else {
                it.toSortedMap(reverseOrder())
            }
        }
        var sortedBooks = listOf<Book>()
        groupedBooks.forEach { (_, books) ->
            sortedBooks = sortedBooks + sortByTitle(books, true)
        }

        val a = sortedBooks + sortByTitle(booksHaveNoRating, true)
        return a
    }

    private fun sortByPublishedDate(books: List<Book>, isAsk: Boolean): List<Book> {
        return if (isAsk) {
            books.sortedBy { it.publishedDate }
        } else {
            books.sortedByDescending { it.publishedDate }
        }
    }
}
