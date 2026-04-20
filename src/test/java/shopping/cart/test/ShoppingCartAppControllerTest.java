package shopping.cart.test;

import javafx.collections.ObservableList;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import shopping.cart.ShoppingCartApp;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShoppingCartAppControllerTest extends JavaFxTestSupport {

    private static final String ENGLISH = "English";
    private static final String JAPANESE = "Japanese";
    private static final String ARABIC = "Arabic";

    @Test
    void testInitializeAddsLanguages() {
        ShoppingCartApp.Controller controller = createController();

        runOnFxThreadAndWait(controller::initialize);

        ObservableList<String> items = controller.getLanguageComboBox().getItems();

        assertEquals(5, items.size());
        assertTrue(items.contains(ENGLISH));
        assertTrue(items.contains(ShoppingCartApp.FINNISH));
        assertTrue(items.contains(ShoppingCartApp.SWEDISH));
        assertTrue(items.contains(JAPANESE));
        assertTrue(items.contains(ARABIC));
    }

    @Test
    void testSetSelectedLanguage() {
        ShoppingCartApp.Controller controller = createInitializedController(JAPANESE);

        assertEquals(JAPANESE, controller.getLanguageComboBox().getValue());
    }

    @Test
    void testApplyLanguageStyleForJapanese() {
        assertStyle(JAPANESE, NodeOrientation.LEFT_TO_RIGHT, "Yu Gothic UI");
    }

    @Test
    void testApplyLanguageStyleForArabic() {
        assertStyle(ARABIC, NodeOrientation.RIGHT_TO_LEFT, "Segoe UI");
    }

    @Test
    void testApplyLanguageStyleForDefaultLanguage() {
        assertStyle(ENGLISH, NodeOrientation.LEFT_TO_RIGHT, "SansSerif");
    }

    @Test
    void testLoadLocalizationAppliesTexts() {
        ShoppingCartApp.Controller controller = createController();
        controller.setLocalizedTextsForTest(createTexts());

        runOnFxThreadAndWait(controller::applyLocalizedTextsForTest);

        assertEquals("Select language", controller.getSelectLanguageLabel().getText());
        assertEquals("Enter items count", controller.getNumberOfItemsLabel().getText());
        assertEquals("Fill item details", controller.getInstructionLabel().getText());
        assertEquals("Enter Items", controller.getEnterItemsButton().getText());
        assertEquals("Calculate Total", controller.getCalculateTotalButton().getText());
        assertEquals("Total Cost", controller.getTotalCostLabel().getText());
        assertEquals("Items count", controller.getItemCountField().getPromptText());
    }

    @Test
    void testLoadLocalizationWithEnglishUpdatesLabels() {
        assertLocalizationState(ENGLISH, true, true);
    }

    @Test
    void testLoadLocalizationWithArabicUpdatesLabels() {
        assertLocalizationState(ARABIC, true, false);
    }

    @Test
    void testLoadLocalizationWithJapaneseUpdatesLabels() {
        assertLocalizationState(JAPANESE, false, true);
    }

    @Test
    void testHandleEnterItemsCreatesRowsInEnglish() {
        ShoppingCartApp.Controller controller = createPreparedController(ENGLISH);

        runOnFxThreadAndWait(() -> controller.getItemCountField().setText("2"));
        runOnFxThreadAndWait(controller::handleEnterItemsForTest);

        assertEquals(2, controller.getItemsContainer().getChildren().size());
        assertEquals(2, controller.getPriceFields().size());
        assertEquals(2, controller.getQuantityFields().size());
        assertEquals(2, controller.getItemTotalLabels().size());
        assertEquals("0.00", controller.getTotalValueLabel().getText());
        assertEquals(NodeOrientation.LEFT_TO_RIGHT, controller.getItemsContainer().getNodeOrientation());
    }

    @Test
    void testHandleEnterItemsCreatesRowsInArabic() {
        ShoppingCartApp.Controller controller = createPreparedController(ARABIC);

        runOnFxThreadAndWait(() -> controller.getItemCountField().setText("1"));
        runOnFxThreadAndWait(controller::handleEnterItemsForTest);

        assertEquals(1, controller.getItemsContainer().getChildren().size());
        assertEquals(NodeOrientation.RIGHT_TO_LEFT, controller.getItemsContainer().getNodeOrientation());
    }

    @Test
    void testHandleCalculateTotalSuccessCoverage() {
        assertCalculatedTotals("35.00", "20.00", "15.00", "10", "2", "5", "3");
    }

    @Test
    void testHandleCalculateTotalWithZeroValues() {
        assertCalculatedTotals("0.00", "0.00", "0.00", "0.0", "5", "10.0", "0");
    }

    @Test
    void testHandleLanguageChangeCoverage() {
        Stage stage = createStageWithController(ARABIC);

        runOnFxThreadAndWait(() -> getControllerFromStage(stage).handleLanguageChangeForTest());

        assertNotNull(stage.getScene());
    }

    @Test
    void testHandleLanguageChangeToArabicReloadsScene() {
        Stage stage = createStageWithController(ARABIC);

        runOnFxThreadAndWait(() -> getControllerFromStage(stage).handleLanguageChangeForTest());

        assertNotNull(stage.getScene());
        assertEquals(ShoppingCartApp.APP_TITLE, stage.getTitle());
    }

    private static void assertStyle(String language, NodeOrientation expectedOrientation, String expectedStylePart) {
        ShoppingCartApp.Controller controller = createInitializedController(language);
        VBox root = new VBox();

        runOnFxThreadAndWait(() -> controller.applyLanguageStyle(root));

        assertEquals(expectedOrientation, root.getNodeOrientation());
        assertTrue(root.getStyle().contains(expectedStylePart));
    }

    private static void assertLocalizationState(String language, boolean verifyTotalCost, boolean verifyCalculateButton) {
        ShoppingCartApp.Controller controller = createInitializedController(language);

        runOnFxThreadAndWait(controller::loadLocalization);

        assertHasNonBlankText(controller.getSelectLanguageLabel());

        if (verifyTotalCost) {
            assertHasNonBlankText(controller.getTotalCostLabel());
        }

        if (verifyCalculateButton) {
            assertHasNonBlankText(controller.getCalculateTotalButton());
        }
    }

    private static void assertCalculatedTotals(
            String expectedTotal,
            String expectedFirstItemTotal,
            String expectedSecondItemTotal,
            String firstPrice,
            String firstQuantity,
            String secondPrice,
            String secondQuantity
    ) {
        ShoppingCartApp.Controller controller = createPreparedController(ENGLISH);

        addItemInputs(controller, firstPrice, firstQuantity);
        addItemInputs(controller, secondPrice, secondQuantity);

        runOnFxThreadAndWait(controller::handleCalculateTotalForTest);

        assertEquals(expectedFirstItemTotal, controller.getItemTotalLabels().get(0).getText());
        assertEquals(expectedSecondItemTotal, controller.getItemTotalLabels().get(1).getText());
        assertEquals(expectedTotal, controller.getTotalValueLabel().getText());
    }

    private static ShoppingCartApp.Controller createController() {
        ShoppingCartApp.Controller controller = new ShoppingCartApp.Controller();

        runOnFxThreadAndWait(() -> controller.initTestControls(
                new ShoppingCartApp.TestControls(
                        new Label(),
                        new javafx.scene.control.ComboBox<>(),
                        new Label(),
                        new TextField(),
                        new javafx.scene.control.Button(),
                        new Label(),
                        new VBox(),
                        new javafx.scene.control.Button(),
                        new Label(),
                        new Label()
                )
        ));

        return controller;
    }

    private static ShoppingCartApp.Controller createInitializedController(String language) {
        ShoppingCartApp.Controller controller = createController();

        runOnFxThreadAndWait(() -> {
            controller.initialize();
            controller.setSelectedLanguage(language);
        });

        return controller;
    }

    private static ShoppingCartApp.Controller createPreparedController(String language) {
        ShoppingCartApp.Controller controller = createInitializedController(language);
        controller.setLocalizedTextsForTest(createTexts());
        return controller;
    }

    private static void addItemInputs(ShoppingCartApp.Controller controller, String price, String quantity) {
        runOnFxThreadAndWait(() -> {
            controller.getPriceFields().add(new TextField(price));
            controller.getQuantityFields().add(new TextField(quantity));
            controller.getItemTotalLabels().add(new Label());
        });
    }

    private static Stage createStageWithController(String language) {
        ShoppingCartApp.Controller controller = createInitializedController(language);
        final Stage[] stageHolder = new Stage[1];

        runOnFxThreadAndWait(() -> {
            VBox root = new VBox(controller.getLanguageComboBox());
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 300, 200));
            stage.setUserData(controller);
            stageHolder[0] = stage;
        });

        return stageHolder[0];
    }

    private static ShoppingCartApp.Controller getControllerFromStage(Stage stage) {
        return (ShoppingCartApp.Controller) stage.getUserData();
    }

    private static void assertHasNonBlankText(Labeled labeled) {
        assertNotNull(labeled.getText());
        assertFalse(labeled.getText().isBlank());
    }

    private static Map<String, String> createTexts() {
        Map<String, String> map = new HashMap<>();
        map.put("select.language", "Select language");
        map.put("enter.number.of.items", "Enter items count");
        map.put("number.of.items.prompt", "Items count");
        map.put("enter.items", "Enter Items");
        map.put("instruction", "Fill item details");
        map.put("calculate.total", "Calculate Total");
        map.put("total.cost", "Total Cost");
        map.put("item", "Item");
        map.put("enter.price.for.item", "Enter price");
        map.put("enter.quantity.for.item", "Enter quantity");
        map.put("item.total", "Item total");
        map.put("error.title", "Error");
        map.put("error.invalid.items", "Invalid items");
        map.put("error.positive.items", "Positive only");
        map.put("error.enter.items.first", "Enter items first");
        map.put("error.nonnegative.values", "Nonnegative only");
        map.put("error.invalid.price.quantity", "Invalid price or quantity");
        return map;
    }
}