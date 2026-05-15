package com.spring.project.dto.web;

import java.math.BigDecimal;

public record BookCardDto(
        String name,
        String author,
        String genre,
        BigDecimal price,
        Integer pages,
        String detailUrl
) {
}
