package org.example.demobookforlease.controller;

import jakarta.validation.Valid;
import org.example.demobookforlease.model.Book;
import org.example.demobookforlease.model.BookStatus;
import org.example.demobookforlease.service.BookService;
import org.example.demobookforlease.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final CategoryService categoryService;

    public BookController(BookService bookService, CategoryService categoryService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listBooks(@RequestParam(required = false) String query,
                            @RequestParam(required = false) Long categoryId,
                            @RequestParam(required = false) BookStatus status,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            Model model) {
        Page<Book> booksPage = bookService.searchBooks(query, categoryId, status, PageRequest.of(page, size));

        model.addAttribute("booksPage", booksPage);
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("statuses", BookStatus.values());
        model.addAttribute("query", query);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("status", status);

        return "books/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("statuses", BookStatus.values());
        return "books/form";
    }

    @PostMapping
    public String saveBook(@Valid @ModelAttribute("book") Book book,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("statuses", BookStatus.values());
            return "books/form";
        }

        if (book.getId() == null) {
            book.setAvailableQuantity(book.getTotalQuantity());
        }

        bookService.saveBook(book);
        redirectAttributes.addFlashAttribute("successMessage", "Lưu thông tin sách thành công!");
        return "redirect:/books";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Book book = bookService.getBookById(id);
        model.addAttribute("book", book);
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("statuses", BookStatus.values());
        return "books/form";
    }
}
