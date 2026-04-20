package shopping.cart;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class CartService {

    private final CartCalculator calculator = new CartCalculator();

    public void saveCart(double[] prices, int[] quantities, String language) {
        String cartSql = "INSERT INTO cart_records (language, total_items, total_cost) VALUES (?, ?, ?)";
        String itemSql = "INSERT INTO cart_items (cart_record_id, item_number, price, quantity, subtotal) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            executeSaveCartTransaction(conn, cartSql, itemSql, prices, quantities, language);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to access database while saving cart.", e);
        }
    }

    private void executeSaveCartTransaction(
            Connection conn,
            String cartSql,
            String itemSql,
            double[] prices,
            int[] quantities,
            String language
    ) throws SQLException {
        try {
            double totalCost = calculator.calculateCartTotal(prices, quantities);
            int totalItems = calculateTotalItems(quantities);
            int cartRecordId = insertCartRecord(conn, cartSql, language, totalItems, totalCost);

            insertCartItems(conn, itemSql, cartRecordId, prices, quantities);
            conn.commit();
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new IllegalStateException("Failed to save cart data.", e);
        }
    }

    private int calculateTotalItems(int[] quantities) {
        int totalItems = 0;
        for (int quantity : quantities) {
            totalItems += quantity;
        }
        return totalItems;
    }

    private int insertCartRecord(
            Connection conn,
            String cartSql,
            String language,
            int totalItems,
            double totalCost
    ) throws SQLException {
        try (PreparedStatement cartStmt = conn.prepareStatement(cartSql, Statement.RETURN_GENERATED_KEYS)) {
            cartStmt.setString(1, language);
            cartStmt.setInt(2, totalItems);
            cartStmt.setDouble(3, totalCost);
            cartStmt.executeUpdate();
            return readGeneratedCartId(cartStmt);
        }
    }

    private int readGeneratedCartId(PreparedStatement cartStmt) throws SQLException {
        try (var generatedKeys = cartStmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
            throw new IllegalStateException("Failed to create cart record.");
        }
    }

    private void insertCartItems(
            Connection conn,
            String itemSql,
            int cartRecordId,
            double[] prices,
            int[] quantities
    ) throws SQLException {
        try (PreparedStatement itemStmt = conn.prepareStatement(itemSql)) {
            itemStmt.setInt(1, cartRecordId);

            for (int i = 0; i < prices.length; i++) {
                double subtotal = calculator.calculateItemTotal(prices[i], quantities[i]);

                itemStmt.setInt(2, i + 1);
                itemStmt.setDouble(3, prices[i]);
                itemStmt.setInt(4, quantities[i]);
                itemStmt.setDouble(5, subtotal);
                itemStmt.addBatch();
            }

            itemStmt.executeBatch();
        }
    }

    private void rollbackQuietly(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException rollbackException) {
            throw new IllegalStateException(
                    "Failed to rollback transaction after cart save error.",
                    rollbackException
            );
        }
    }
}