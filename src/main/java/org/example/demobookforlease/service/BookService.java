package org.example.demobookforlease.service;

import org.example.demobookforlease.exception.ResourceNotFoundException;
import org.example.demobookforlease.model.Book;
import org.example.demobookforlease.model.BookStatus;
import org.example.demobookforlease.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public Page<Book> searchBooks(String query, Long categoryId, BookStatus status, Pageable pageable) {
        return bookRepository.searchBooks(query, categoryId, status, pageable);
    }

    @Transactional(readOnly = true)
    public List<Book> getAvailableBooks() {
        return bookRepository.findByStatusAndAvailableQuantityGreaterThan(BookStatus.AVAILABLE, 0);
    }

    @Transactional(readOnly = true)
    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách có mã ID: " + id));
    }

    @Transactional
    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    @Transactional
    public void deleteBook(Long id) {
        Book book = getBookById(id);
        bookRepository.delete(book);
    }
}
