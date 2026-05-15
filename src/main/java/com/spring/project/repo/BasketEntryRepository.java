package com.spring.project.repo;

import com.spring.project.model.BasketEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BasketEntryRepository extends JpaRepository<BasketEntry, Long> {

    List<BasketEntry> findByClient_EmailOrderByIdAsc(String clientEmail);

    Optional<BasketEntry> findByClient_EmailAndBook_Name(String clientEmail, String bookName);

    void deleteByClient_EmailAndBook_Name(String clientEmail, String bookName);

    void deleteByClient_Email(String clientEmail);
}
