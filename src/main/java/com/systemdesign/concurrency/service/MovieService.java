package com.systemdesign.concurrency.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class MovieService {
    public MovieService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final JdbcTemplate jdbcTemplate;
    @Transactional
    public void performTransactionalOperation(int threadId) {
        try {

            Map<String, Object> row = jdbcTemplate.queryForMap("SELECT * FROM  movie_seats WHERE user_id is NULL ORDER BY id LIMIT 1 FOR UPDATE SKIP LOCKED");

            List<Map.Entry<String, Object>> entryList = row.entrySet().stream()
                    .toList();
            System.out.println("Thread " + threadId + " selected seat: " + entryList.get(1).getValue());
            jdbcTemplate.update("UPDATE movie_seats SET user_id = ? where seat_number = ?", threadId,entryList.get(1).getValue());
        } catch (Exception e) {
            System.out.println("Exception in thread " + threadId + ": " + e.getMessage());
            throw e; // Ensure transaction rollback
        }
    }

    public void executeInThreads() {
        ExecutorService executorService = Executors.newFixedThreadPool(25); // Pool with 10 threads

        for (int i = 1; i <= 25; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                try {
                    performTransactionalOperation(threadId);
                } catch (Exception e) {
                    // Handle exceptions if needed
                }
            });
        }

        // Shutdown the executor service
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}
