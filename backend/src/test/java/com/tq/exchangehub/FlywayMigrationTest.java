package com.tq.exchangehub;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class FlywayMigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldApplySuspensionColumnAndDefaultValue() throws SQLException {
        boolean columnExists = columnExists("USER_ACCOUNTS", "IS_SUSPENDED");
        assertThat(columnExists).isTrue();

        Integer totalAccounts = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_accounts", Integer.class);
        Integer defaultFalseCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_accounts WHERE is_suspended = FALSE", Integer.class);

        assertThat(defaultFalseCount).isEqualTo(totalAccounts);
    }

    private boolean columnExists(String tableName, String columnName) throws SQLException {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet columns = metaData.getColumns(null, null, tableName, columnName)) {
                return columns.next();
            }
        }
    }
}
