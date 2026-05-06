package com.spring.project.controller.web;

import com.spring.project.dto.BookDTO;
import com.spring.project.model.enums.AgeGroup;
import com.spring.project.model.enums.Language;
import com.spring.project.service.BookPaging;
import com.spring.project.service.BookService;
import com.spring.project.service.ClientService;
import com.spring.project.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/app/employee")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeWebController {

    private final BookService bookService;
    private final ClientService clientService;
    private final EmployeeService employeeService;

    @GetMapping
    public String dashboard() {
        return "employee/dashboard";
    }

    @GetMapping("/books")
    public String books(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minPages,
            @RequestParam(required = false) Integer maxPages,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        Page<BookDTO> bookPage = bookService.searchBooks(
                q, minPrice, maxPrice, minPages, maxPages, BookPaging.pageable(page));
        model.addAttribute("bookPage", bookPage);
        model.addAttribute("q", q != null ? q : "");
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("minPages", minPages);
        model.addAttribute("maxPages", maxPages);
        return "employee/books";
    }

    @GetMapping("/books/new")
    public String newBookForm(Model model) {
        BookDTO dto = new BookDTO();
        dto.setAgeGroup(AgeGroup.ADULT);
        dto.setLanguage(Language.ENGLISH);
        model.addAttribute("bookForm", dto);
        model.addAttribute("editMode", false);
        return "employee/book-form";
    }

    @PostMapping("/books/save")
    public String saveBook(
            @RequestParam(value = "originalName", required = false) String originalName,
            @Valid @ModelAttribute("bookForm") BookDTO dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes ra) {
            
        // Logical validation 1: Children's books shouldn't be too long
        if (dto.getAgeGroup() == AgeGroup.CHILD && dto.getPages() != null && dto.getPages() > 300) {
            bindingResult.rejectValue("pages", "bookForm.pages.childrenLimit", "Children's books cannot exceed 300 pages");
        }
        
        // Logical validation 2: Publication date cannot be in the future
        if (dto.getPublicationDate() != null && dto.getPublicationDate().isAfter(java.time.LocalDate.now())) {
            bindingResult.rejectValue("publicationDate", "bookForm.publicationDate.future", "Publication date cannot be in the future");
        }

        boolean editMode = originalName != null && !originalName.isBlank();
        if (bindingResult.hasErrors()) {
            model.addAttribute("editMode", editMode);
            if (editMode) {
                model.addAttribute("originalName", originalName);
            }
            return "employee/book-form";
        }
        
        try {
            if (editMode) {
                bookService.updateBookByName(originalName, dto);
                ra.addFlashAttribute("flashSuccess", "employee.book_updated");
            } else {
                bookService.addBook(dto);
                ra.addFlashAttribute("flashSuccess", "employee.book_created");
            }
        } catch (com.spring.project.exception.AlreadyExistException e) {
            bindingResult.rejectValue("name", "bookForm.name.duplicate", "A book with this name already exists in the catalog");
            model.addAttribute("editMode", editMode);
            if (editMode) {
                model.addAttribute("originalName", originalName);
            }
            return "employee/book-form";
        }
        
        return "redirect:/app/employee/books";
    }

    @GetMapping("/books/edit")
    public String editBookForm(@RequestParam("name") String name, Model model) {
        model.addAttribute("bookForm", bookService.getBookByName(name));
        model.addAttribute("originalName", name);
        model.addAttribute("editMode", true);
        return "employee/book-form";
    }

    @PostMapping("/books/delete")
    public String deleteBook(@RequestParam("name") String name, RedirectAttributes ra) {
        bookService.deleteBookByName(name);
        ra.addFlashAttribute("flashSuccess", "employee.book_deleted");
        return "redirect:/app/employee/books";
    }

    @GetMapping("/clients")
    public String clients(Model model) {
        model.addAttribute("clients", clientService.getAllClients());
        return "employee/clients";
    }

    @PostMapping("/clients/block")
    public String block(@RequestParam String email, RedirectAttributes ra) {
        employeeService.blockClient(email);
        ra.addFlashAttribute("flashSuccess", "employee.client_blocked");
        return "redirect:/app/employee/clients";
    }

    @PostMapping("/clients/unblock")
    public String unblock(@RequestParam String email, RedirectAttributes ra) {
        employeeService.unblockClient(email);
        ra.addFlashAttribute("flashSuccess", "employee.client_unblocked");
        return "redirect:/app/employee/clients";
    }
}
