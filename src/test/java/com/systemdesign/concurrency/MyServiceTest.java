package com.systemdesign.concurrency;

import com.systemdesign.concurrency.service.MovieService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
public class MyServiceTest {

    @Autowired
    private MovieService myService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testExecuteInThreads() {
        myService.executeInThreads();

        // Verify the updates
        jdbcTemplate.query("SELECT * FROM movie_seats", (rs, rowNum) -> {
            System.out.println("ID: " + rs.getInt("id") + ", Seat Number: " + rs.getInt("seat_number")+" User Id: "+rs.getInt("user_id"));
            return null;
        });
    }
}
