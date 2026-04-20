package shopping.cart;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ShoppingCartApp extends Application {

    public static final String APP_TITLE = "Kumudu Nallaperuma / Shopping Cart App";
    public static final String ENGLISH = "English";
    public static final String FINNISH = "Finnish";
    public static final String SWEDISH = "Swedish";
    public static final String JAPANESE = "Japanese";
    public static final String ARABIC = "Arabic";

    @Override
    public void start(Stage stage) throws Exception {
        loadScene(stage, ENGLISH);
        stage.show();
    }

    public void loadScene(Stage stage, String selectedLanguage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                ShoppingCartApp.class.getResource("/main-view.fxml")
        );

        Parent root = loader.load();

        Controller controller = loader.getController();
        controller.setSelectedLanguage(selectedLanguage);
        controller.loadLocalization();
        controller.applyLanguageStyle(root);

        Scene scene = new Scene(root, 900, 650);
        stage.setTitle(APP_TITLE);
        stage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }

    public record TestControls(
            Label selectLanguageLabel,
            ComboBox<String> languageComboBox,
            Label numberOfItemsLabel,
            TextField itemCountField,
            Button enterItemsButton,
            Label instructionLabel,
            VBox itemsContainer,
            Button calculateTotalButton,
            Label totalCostLabel,
            Label totalValueLabel
    ) {
    }

    public static class Controller {

        @FXML
        private Label selectLanguageLabel;

        @FXML
        private ComboBox<String> languageComboBox;

        @FXML
        private Label numberOfItemsLabel;

        @FXML
        private TextField itemCountField;

        @FXML
        private Button enterItemsButton;

        @FXML
        private Label instructionLabel;

        @FXML
        private VBox itemsContainer;

        @FXML
        private Button calculateTotalButton;

        @FXML
        private Label totalCostLabel;

        @FXML
        private Label totalValueLabel;

        private final List<TextField> priceFields = new ArrayList<>();
        private final List<TextField> quantityFields = new ArrayList<>();
        private final List<Label> itemTotalLabels = new ArrayList<>();

        private final CartCalculator calculator = new CartCalculator();
        private final CartService cartService = new CartService();
        private final LocalizationService localizationService = new LocalizationService();
        private final DecimalFormat df = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.US));

        private Map<String, String> localizedTexts = new HashMap<>();

        @FXML
        public void initialize() {
            languageComboBox.setItems(FXCollections.observableArrayList(
                    ENGLISH, FINNISH, SWEDISH, JAPANESE, ARABIC
            ));
        }

        public void setSelectedLanguage(String language) {
            languageComboBox.setValue(language);
        }

        public void loadLocalization() {
            String languageCode = mapLanguageToCode(languageComboBox.getValue());
            localizedTexts = localizationService.getLocalizedStrings(languageCode);
            applyLocalizedTexts();
        }

        private void applyLocalizedTexts() {
            selectLanguageLabel.setText(getText("select.language"));
            numberOfItemsLabel.setText(getText("enter.number.of.items"));
            itemCountField.setPromptText(getText("number.of.items.prompt"));
            enterItemsButton.setText(getText("enter.items"));
            instructionLabel.setText(getText("instruction"));
            calculateTotalButton.setText(getText("calculate.total"));
            totalCostLabel.setText(getText("total.cost"));
        }

        public void applyLanguageStyle(Parent root) {
            String code = mapLanguageToCode(languageComboBox.getValue());

            if ("ja".equals(code)) {
                root.setStyle("-fx-font-family: 'Yu Gothic UI', 'Meiryo', 'MS Gothic';");
                root.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            } else if ("ar".equals(code)) {
                root.setStyle("-fx-font-family: 'Segoe UI', 'Tahoma', 'Arial Unicode MS';");
                root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            } else {
                root.setStyle("-fx-font-family: 'Segoe UI', 'Arial', 'SansSerif';");
                root.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            }
        }

        @FXML
        private void handleLanguageChange() {
            String selected = languageComboBox.getValue();

            try {
                FXMLLoader loader = new FXMLLoader(
                        ShoppingCartApp.class.getResource("/main-view.fxml")
                );

                Parent root = loader.load();

                Controller controller = loader.getController();
                controller.setSelectedLanguage(selected);
                controller.loadLocalization();
                controller.applyLanguageStyle(root);

                Scene scene = new Scene(root, 900, 650);
                Stage stage = (Stage) languageComboBox.getScene().getWindow();
                stage.setTitle(APP_TITLE);
                stage.setScene(scene);

            } catch (IOException e) {
                showError("Could not change language.");
            }
        }

        @FXML
        private void handleEnterItems() {
            itemsContainer.getChildren().clear();
            priceFields.clear();
            quantityFields.clear();
            itemTotalLabels.clear();
            totalValueLabel.setText("0.00");

            int itemCount;

            try {
                itemCount = Integer.parseInt(itemCountField.getText().trim());
                if (itemCount <= 0) {
                    showError(getText("error.positive.items"));
                    return;
                }
            } catch (NumberFormatException e) {
                showError(getText("error.invalid.items"));
                return;
            }

            for (int i = 0; i < itemCount; i++) {
                Label itemLabel = new Label(getText("item") + " " + (i + 1) + ":");

                TextField priceField = new TextField();
                priceField.setPromptText(getText("enter.price.for.item"));
                priceField.setPrefWidth(180);

                TextField quantityField = new TextField();
                quantityField.setPromptText(getText("enter.quantity.for.item"));
                quantityField.setPrefWidth(180);

                Label itemTotalTextLabel = new Label(getText("item.total"));
                Label itemTotalValueLabel = new Label("0.00");

                HBox row = new HBox(10);
                row.getChildren().addAll(
                        itemLabel,
                        priceField,
                        quantityField,
                        itemTotalTextLabel,
                        itemTotalValueLabel
                );

                if (isArabicSelected()) {
                    row.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                } else {
                    row.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                }

                itemsContainer.getChildren().add(row);
                priceFields.add(priceField);
                quantityFields.add(quantityField);
                itemTotalLabels.add(itemTotalValueLabel);
            }

            if (isArabicSelected()) {
                itemsContainer.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            } else {
                itemsContainer.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            }
        }

        @FXML
        private void handleCalculateTotal() {
            if (priceFields.isEmpty()) {
                showError(getText("error.enter.items.first"));
                return;
            }

            double[] prices = new double[priceFields.size()];
            int[] quantities = new int[quantityFields.size()];

            for (int i = 0; i < priceFields.size(); i++) {
                try {
                    double price = Double.parseDouble(priceFields.get(i).getText().trim());
                    int quantity = Integer.parseInt(quantityFields.get(i).getText().trim());

                    if (price < 0 || quantity < 0) {
                        showError(getText("error.nonnegative.values"));
                        return;
                    }

                    prices[i] = price;
                    quantities[i] = quantity;

                    double itemTotal = calculator.calculateItemTotal(price, quantity);
                    itemTotalLabels.get(i).setText(df.format(itemTotal));

                } catch (NumberFormatException e) {
                    showError(getText("error.invalid.price.quantity") + " " + (i + 1));
                    return;
                }
            }

            double total = calculator.calculateCartTotal(prices, quantities);
            totalValueLabel.setText(df.format(total));

            String languageCode = mapLanguageToCode(languageComboBox.getValue());
            cartService.saveCart(prices, quantities, languageCode);
        }

        private String mapLanguageToCode(String language) {
            return switch (language) {
                case FINNISH -> "fi";
                case SWEDISH -> "sv";
                case JAPANESE -> "ja";
                case ARABIC -> "ar";
                default -> "en";
            };
        }

        private String getText(String key) {
            return localizedTexts.getOrDefault(key, key);
        }

        private boolean isArabicSelected() {
            return ARABIC.equals(languageComboBox.getValue());
        }

        private void showError(String message) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(getText("error.title"));
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }

        public void initTestControls(TestControls controls) {
            this.selectLanguageLabel = controls.selectLanguageLabel();
            this.languageComboBox = controls.languageComboBox();
            this.numberOfItemsLabel = controls.numberOfItemsLabel();
            this.itemCountField = controls.itemCountField();
            this.enterItemsButton = controls.enterItemsButton();
            this.instructionLabel = controls.instructionLabel();
            this.itemsContainer = controls.itemsContainer();
            this.calculateTotalButton = controls.calculateTotalButton();
            this.totalCostLabel = controls.totalCostLabel();
            this.totalValueLabel = controls.totalValueLabel();
        }

        public void setLocalizedTextsForTest(Map<String, String> localizedTexts) {
            this.localizedTexts = localizedTexts;
        }

        public void applyLocalizedTextsForTest() {
            applyLocalizedTexts();
        }

        public void handleEnterItemsForTest() {
            handleEnterItems();
        }

        public void handleCalculateTotalForTest() {
            handleCalculateTotal();
        }

        public void handleLanguageChangeForTest() {
            handleLanguageChange();
        }

        public Label getSelectLanguageLabel() {
            return selectLanguageLabel;
        }

        public ComboBox<String> getLanguageComboBox() {
            return languageComboBox;
        }

        public Label getNumberOfItemsLabel() {
            return numberOfItemsLabel;
        }

        public TextField getItemCountField() {
            return itemCountField;
        }

        public Button getEnterItemsButton() {
            return enterItemsButton;
        }

        public Label getInstructionLabel() {
            return instructionLabel;
        }

        public VBox getItemsContainer() {
            return itemsContainer;
        }

        public Button getCalculateTotalButton() {
            return calculateTotalButton;
        }

        public Label getTotalCostLabel() {
            return totalCostLabel;
        }

        public Label getTotalValueLabel() {
            return totalValueLabel;
        }

        public List<TextField> getPriceFields() {
            return priceFields;
        }

        public List<TextField> getQuantityFields() {
            return quantityFields;
        }

        public List<Label> getItemTotalLabels() {
            return itemTotalLabels;
        }

        public Map<String, String> getLocalizedTexts() {
            return localizedTexts;
        }
    }
}