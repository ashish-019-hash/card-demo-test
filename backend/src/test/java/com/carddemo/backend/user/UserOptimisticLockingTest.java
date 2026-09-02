package com.carddemo.backend.user;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserOptimisticLockingTest {
    @Autowired UserRepository users;
    @Autowired EntityManager entityManager;

    @Test
    void databaseVersionRejectsAStaleDetachedEntity() {
        User created = users.saveAndFlush(new User("LOCKTEST", "Lock", "Test", "hash", UserRole.REGULAR));
        entityManager.clear();
        User stale = users.findById(created.getUserId()).orElseThrow();
        entityManager.detach(stale);

        User current = users.findById(created.getUserId()).orElseThrow();
        current.update("Current", "Test", "hash", UserRole.REGULAR);
        users.saveAndFlush(current);
        entityManager.clear();

        stale.update("Stale", "Test", "hash", UserRole.REGULAR);
        assertThatThrownBy(() -> users.saveAndFlush(stale))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }
}
