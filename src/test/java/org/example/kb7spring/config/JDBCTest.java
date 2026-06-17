package org.example.kb7spring.config;

import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.DriverManager;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
public class JDBCTest {
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("JDBC 드라이버연결이된다.")
    public void testConnection() {
        String url= "jdbc:mysql://localhost:3306/scoula_db";
        try(Connection con = DriverManager.getConnection(url, "scoula", "1234"))  {
            log.info("Connection: {}", con);
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
}