package com.spring.project.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ensures SQL seed passwords in sql/sql.sql match BCrypt hashes (cost 10, Spring-compatible).
 * Demo login for all seeded rows: email from data + plaintext {@value #SEED_PLAINTEXT_PASSWORD}.
 */
class SeedBcryptHashesMatchTest {

    static final String SEED_PLAINTEXT_PASSWORD = "password123";

    /**
     * Same BCrypt string is used for every PASSWORD value in sql/sql.sql seed inserts.
     * BCrypt for {@value #SEED_PLAINTEXT_PASSWORD} (cost factor 10).
     */
    static final String SEED_BCRYPT_HASH =
            "$2a$10$kypbnGGCpJ7UQlysnqzJG.6H.dUewn7UPVWA3Ip.E.8U4jlVnFNnu";

    @Test
    void seedHashMatchesPassword123() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        assertTrue(encoder.matches(SEED_PLAINTEXT_PASSWORD, SEED_BCRYPT_HASH),
                "Update SEED_BCRYPT_HASH in sql/sql.sql or adjust password to match encoder output");
    }
}
