package com.spring.project.dto.web;

import java.util.List;

public record BookSearchPageDto(
        List<BookCardDto> content,
        long totalElements,
        int totalPages,
        int page,
        boolean hasNext,
        boolean hasPrevious
) {
}
