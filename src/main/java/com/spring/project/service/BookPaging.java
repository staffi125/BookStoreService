package com.spring.project.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Fixed-size book catalog pages: {@value #PAGE_SIZE} items, sorted by title (A→Z).
 */
public final class BookPaging {

    public static final int PAGE_SIZE = 6;

    private BookPaging() {
    }

    public static Pageable pageable(int page) {
        int safePage = Math.max(page, 0);
        return PageRequest.of(safePage, PAGE_SIZE, Sort.by(Sort.Direction.ASC, "name"));
    }
}
