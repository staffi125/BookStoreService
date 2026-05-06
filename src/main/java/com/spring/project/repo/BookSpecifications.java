package com.spring.project.repo;

import com.spring.project.model.Book;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class BookSpecifications {

    private BookSpecifications() {
    }

    /**
     * Combined catalog filter: optional text search (title, author, or genre) plus optional numeric ranges.
     */
    public static Specification<Book> catalogFilter(
            String textQuery,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer minPages,
            Integer maxPages) {
        return Specification.where(textMatches(textQuery))
                .and(priceBetween(minPrice, maxPrice))
                .and(pagesBetween(minPages, maxPages));
    }

    /**
     * Case-insensitive match on name, author, or genre. Empty or blank query matches all rows.
     */
    public static Specification<Book> textMatches(String raw) {
        return (root, query, cb) -> {
            if (raw == null || raw.isBlank()) {
                return cb.conjunction();
            }
            String term = "%" + sanitize(raw) + "%";
            var name = cb.like(cb.lower(root.get("name")), term);
            var author = cb.like(cb.lower(root.get("author")), term);
            var genre = cb.like(cb.lower(root.get("genre")), term);
            return cb.or(name, author, genre);
        };
    }

    private static Specification<Book> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            Path<BigDecimal> path = root.<BigDecimal>get("price");
            List<Predicate> parts = new ArrayList<>(2);
            if (min != null) {
                parts.add(cb.greaterThanOrEqualTo(path, min));
            }
            if (max != null) {
                parts.add(cb.lessThanOrEqualTo(path, max));
            }
            if (parts.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(parts.toArray(Predicate[]::new));
        };
    }

    private static Specification<Book> pagesBetween(Integer min, Integer max) {
        return (root, query, cb) -> {
            if (min == null && max == null) {
                return cb.conjunction();
            }
            Path<Integer> path = root.<Integer>get("pages");
            List<Predicate> parts = new ArrayList<>(3);
            parts.add(cb.isNotNull(path));
            if (min != null) {
                parts.add(cb.greaterThanOrEqualTo(path, min));
            }
            if (max != null) {
                parts.add(cb.lessThanOrEqualTo(path, max));
            }
            return cb.and(parts.toArray(Predicate[]::new));
        };
    }

    private static String sanitize(String s) {
        return s.trim().toLowerCase().replace("%", "").replace("_", "");
    }
}
