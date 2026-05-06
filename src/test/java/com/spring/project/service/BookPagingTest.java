package com.spring.project.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BookPagingTest {

    @Test
    void pageableFixedSizeAndSortByNameAsc() {
        Pageable p = BookPaging.pageable(0);
        assertEquals(0, p.getPageNumber());
        assertEquals(BookPaging.PAGE_SIZE, p.getPageSize());
        assertEquals(Sort.Direction.ASC, p.getSort().getOrderFor("name").getDirection());
    }

    @Test
    void pageableNeverNegativePage() {
        Pageable p = BookPaging.pageable(-3);
        assertEquals(0, p.getPageNumber());
    }
}
