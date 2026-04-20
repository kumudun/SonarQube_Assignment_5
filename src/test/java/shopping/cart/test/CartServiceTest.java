package shopping.cart.test;

import org.junit.jupiter.api.Test;
import shopping.cart.CartService;
import shopping.cart.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartServiceTest {

    private static final String TOTAL_ITEMS = "total_items";
    private static final String TOTAL_COST = "total_cost";

    private final CartService cartService = new CartService();

    @Test
    void testSaveCartInsertsCartRecordAndItems() throws Exception {
        double[] prices = {10.0, 5.0};
        int[] quantities = {2, 3};
        String language = "en";

        cartService.saveCart(prices, quantities, language);

        try (Connection connection = DatabaseConnection.getConnection()) {
            int cartRecordId;
            int totalItems;
            double totalCost;

            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT id, total_items, total_cost FROM cart_records WHERE language = ? ORDER BY id DESC LIMIT 1"
            )) {
                stmt.setString(1, language);

                try (ResultSet rs = stmt.executeQuery()) {
                    assertTrue(rs.next());
                    cartRecordId = rs.getInt("id");
                    totalItems = rs.getInt(TOTAL_ITEMS);
                    totalCost = rs.getDouble(TOTAL_COST);
                }
            }

            assertEquals(5, totalItems);
            assertEquals(35.0, totalCost, 0.001);

            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT COUNT(*) FROM cart_items WHERE cart_record_id = ?"
            )) {
                stmt.setInt(1, cartRecordId);

                try (ResultSet rs = stmt.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(2, rs.getInt(1));
                }
            }
        }
    }

    @Test
    void testSaveCartWithSingleItem() throws Exception {
        double[] prices = {7.5};
        int[] quantities = {4};
        String language = "fi";

        cartService.saveCart(prices, quantities, language);

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "SELECT total_items, total_cost FROM cart_records WHERE language = ? ORDER BY id DESC LIMIT 1"
             )) {

            stmt.setString(1, language);

            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(4, rs.getInt(TOTAL_ITEMS));
                assertEquals(30.0, rs.getDouble(TOTAL_COST), 0.001);
            }
        }
    }

    @Test
    void testSaveCartWithEmptyArrays() throws Exception {
        double[] prices = {};
        int[] quantities = {};
        String language = "sv";

        cartService.saveCart(prices, quantities, language);

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "SELECT total_items, total_cost FROM cart_records WHERE language = ? ORDER BY id DESC LIMIT 1"
             )) {

            stmt.setString(1, language);

            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt(TOTAL_ITEMS));
                assertEquals(0.0, rs.getDouble(TOTAL_COST), 0.001);
            }
        }
    }
}