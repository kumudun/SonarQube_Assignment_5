package shopping.cart.test;

import org.junit.jupiter.api.Test;
import shopping.cart.CartCalculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CartCalculatorTest {

    private static final String ARRAY_LENGTH_ERROR_MESSAGE = "Prices and quantities must have the same length.";
    private static final String NULL_ARRAY_ERROR_MESSAGE = "Prices and quantities cannot be null.";

    private final CartCalculator calculator = new CartCalculator();

    @Test
    void testCalculateItemTotal() {
        double result = calculator.calculateItemTotal(10.0, 3);
        assertEquals(30.0, result, 0.001);
    }

    @Test
    void testCalculateItemTotalWithZeroPrice() {
        double result = calculator.calculateItemTotal(0.0, 5);
        assertEquals(0.0, result, 0.001);
    }

    @Test
    void testCalculateItemTotalWithZeroQuantity() {
        double result = calculator.calculateItemTotal(25.0, 0);
        assertEquals(0.0, result, 0.001);
    }

    @Test
    void testCalculateItemTotalWithDecimalPrice() {
        double result = calculator.calculateItemTotal(2.5, 4);
        assertEquals(10.0, result, 0.001);
    }

    @Test
    void testCalculateCartTotal() {
        double[] prices = {10.0, 5.5, 7.0};
        int[] quantities = {2, 4, 1};

        double result = calculator.calculateCartTotal(prices, quantities);
        assertEquals(49.0, result, 0.001);
    }

    @Test
    void testCalculateCartTotalSingleItem() {
        double[] prices = {15.0};
        int[] quantities = {2};

        double result = calculator.calculateCartTotal(prices, quantities);
        assertEquals(30.0, result, 0.001);
    }

    @Test
    void testCalculateCartTotalWithZeroValues() {
        double[] prices = {0.0, 10.0};
        int[] quantities = {5, 0};

        double result = calculator.calculateCartTotal(prices, quantities);
        assertEquals(0.0, result, 0.001);
    }

    @Test
    void testCalculateCartTotalWithDecimals() {
        double[] prices = {2.5, 3.2};
        int[] quantities = {2, 3};

        double result = calculator.calculateCartTotal(prices, quantities);
        assertEquals(14.6, result, 0.001);
    }

    @Test
    void testEmptyCartTotal() {
        double[] prices = {};
        int[] quantities = {};
        double result = calculator.calculateCartTotal(prices, quantities);
        assertEquals(0.0, result, 0.001);
    }

    @Test
    void testDifferentArrayLengths() {
        double[] prices = {10.0, 20.0};
        int[] quantities = {1};

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateCartTotal(prices, quantities)
        );

        assertEquals(ARRAY_LENGTH_ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    void testNullArrays() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateCartTotal(null, null)
        );

        assertEquals(NULL_ARRAY_ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    void testNullPricesOnly() {
        int[] quantities = {1, 2};

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateCartTotal(null, quantities)
        );

        assertEquals(NULL_ARRAY_ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    void testNullQuantitiesOnly() {
        double[] prices = {10.0, 20.0};

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateCartTotal(prices, null)
        );

        assertEquals(NULL_ARRAY_ERROR_MESSAGE, exception.getMessage());
    }
}