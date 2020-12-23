package com.abhishek.utils.collection.cache;

import static org.assertj.core.api.Assertions.*;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@Slf4j
@SpringBootTest("logging.level.com.abhishek.utils=TRACE")
class LRUCacheTest {

    private LRUCache<Integer, String> cache;

    private String fetchFromSource(Integer i) {
        log.info("Fetching from source for key : {}", i);
        return String.valueOf(i)+ " : " + new Date();
    }

    @BeforeEach
    void setUp() {
        this.cache = new LRUCache<Integer, String>(this::fetchFromSource, 3);
    }

    @AfterEach
    void tearDown() {
        this.cache = null;
    }

    @Test
    void get() {
        assertThat(cache.get(1)).startsWith("1");
        log.info("Cache : {}", cache);

        assertThat(cache.get(1)).startsWith("1");
        log.info("Cache : {}", cache);
        assertThat(cache.get(2)).startsWith("2");
        log.info("Cache : {}", cache);
        assertThat(cache.get(3)).startsWith("3");
        log.info("Cache : {}", cache);
        assertThat(cache.get(4)).startsWith("4");
        log.info("Cache : {}", cache);

        assertThat(cache.get(1)).startsWith("1");
        log.info("Cache : {}", cache);
        assertThat(cache.get(2)).startsWith("2");
        log.info("Cache : {}", cache);
        assertThat(cache.get(3)).startsWith("3");
        log.info("Cache : {}", cache);
        assertThat(cache.get(2)).startsWith("2");
        log.info("Cache : {}", cache);
    }
}
