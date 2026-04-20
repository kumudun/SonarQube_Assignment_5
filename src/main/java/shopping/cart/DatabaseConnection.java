package shopping.cart;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/shopping_cart_localization?useSSL=false&serverTimezone=UTC";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        if (user == null || password == null) {
            throw new SQLException("Database credentials are not set in environment variables.");
        }

        return DriverManager.getConnection(URL, user, password);
    }
}