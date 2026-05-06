package com.spring.project.controller.web;

import com.spring.project.dto.BasketLineView;
import com.spring.project.dto.BookItemDTO;
import com.spring.project.service.BasketService;
import com.spring.project.service.BookService;
import com.spring.project.service.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/app/basket")
@RequiredArgsConstructor
public class BasketWebController {

    private final BasketService basketService;
    private final BookService bookService;
    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("hasRole('CLIENT')")
    public String view(HttpSession session, Model model) {
        List<BasketLineView> lines = buildBasketLines(session);
        model.addAttribute("lines", lines);
        model.addAttribute("basketEmpty", lines.isEmpty());
        model.addAttribute("basketTotal", sumLines(lines));
        return "basket";
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('CLIENT')")
    public String add(
            HttpSession session,
            @RequestParam String bookName,
            @RequestParam(defaultValue = "1") int quantity,
            RedirectAttributes ra) {
        if (quantity < 1) {
            quantity = 1;
        }
        basketService.addItem(session, bookName, quantity);
        ra.addFlashAttribute("flashSuccess", "basket.added");
        return "redirect:/app/basket";
    }

    @PostMapping("/remove")
    @PreAuthorize("hasRole('CLIENT')")
    public String remove(HttpSession session, @RequestParam String bookName) {
        basketService.removeItem(session, bookName);
        return "redirect:/app/basket";
    }

    @PostMapping("/clear")
    @PreAuthorize("hasRole('CLIENT')")
    public String clear(HttpSession session) {
        basketService.clear(session);
        return "redirect:/app/basket";
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('CLIENT')")
    public String checkout(
            @AuthenticationPrincipal UserDetails principal,
            HttpSession session,
            RedirectAttributes ra) {
        try {
            orderService.checkoutFromBasket(session, principal.getUsername());
            ra.addFlashAttribute("flashSuccess", "checkout.done");
            return "redirect:/app/profile";
        } catch (AccessDeniedException e) {
            ra.addFlashAttribute("flashError", "checkout.blocked");
            return "redirect:/app/basket";
        } catch (IllegalStateException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("Insufficient balance")) {
                ra.addFlashAttribute("flashError", "checkout.insufficient_balance");
                return "redirect:/app/basket";
            }
            ra.addFlashAttribute("flashError", "checkout.empty");
            return "redirect:/app/basket";
        }
    }

    private List<BasketLineView> buildBasketLines(HttpSession session) {
        List<BasketLineView> lines = new ArrayList<>();
        for (BookItemDTO item : basketService.getItems(session)) {
            var book = bookService.getBookByName(item.getBookName());
            BigDecimal unit = book.getPrice();
            BigDecimal lineTotal = unit.multiply(BigDecimal.valueOf(item.getQuantity()));
            lines.add(new BasketLineView(item.getBookName(), item.getQuantity(), unit, lineTotal));
        }
        return lines;
    }

    private static BigDecimal sumLines(List<BasketLineView> lines) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BasketLineView line : lines) {
            sum = sum.add(line.getLineTotal());
        }
        return sum;
    }
}
