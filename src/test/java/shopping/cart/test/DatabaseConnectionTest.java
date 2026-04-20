package shopping.cart.test;

import org.junit.jupiter.api.Test;
import shopping.cart.DatabaseConnection;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DatabaseConnectionTest {

    @Test
    void testGetConnectionReturnsOpenConnection() throws Exception {
        try (Connection connection = DatabaseConnection.getConnection()) {
            assertNotNull(connection);
            assertFalse(connection.isClosed());
        }
    }
}