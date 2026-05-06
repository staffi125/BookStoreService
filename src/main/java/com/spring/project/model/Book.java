package com.spring.project.model;

import com.spring.project.model.enums.AgeGroup;
import com.spring.project.model.enums.Language;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "genre")
    private String genre;
    @Enumerated(EnumType.STRING)
    @Column(name = "age_group")
    private AgeGroup ageGroup;
    @Column(name = "price")
    private BigDecimal price;
    @Column(name = "publication_year")
    private LocalDate publicationDate;
    @Column(name = "author")
    private String author;
    @Column(name = "number_of_pages")
    private Integer pages;
    @Column(name = "characteristics")
    private String characteristics;
    @Column(name = "description")
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(name = "language")
    private Language language;

    public Book(Long id, String name, String genre, AgeGroup ageGroup, BigDecimal price, LocalDate publicationDate,
                String author, Integer pages, String characteristics, String description, Language language) {
        this.id = id;
        this.name = name;
        this.genre = genre;
        this.ageGroup = ageGroup;
        this.price = price;
        this.publicationDate = publicationDate;
        this.author = author;
        this.pages = pages;
        this.characteristics = characteristics;
        this.description = description;
        this.language = language;
    }
}
