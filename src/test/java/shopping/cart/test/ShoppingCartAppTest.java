package shopping.cart.test;

import org.junit.jupiter.api.Test;
import shopping.cart.ShoppingCartApp;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShoppingCartAppTest extends JavaFxTestSupport {

    @Test
    void testLoadSceneSetsStageTitleAndScene() {
        verifyStageInitialization(false);
    }

    @Test
    void testStartSetsStageTitleAndScene() {
        verifyStageInitialization(true);
    }

    private static void verifyStageInitialization(boolean startApplication) {
        ShoppingCartApp app = new ShoppingCartApp();
        var stage = createStage();

        runOnFxThreadAndWait(() -> executeAppAction(app, stage, startApplication));

        assertEquals(ShoppingCartApp.APP_TITLE, stage.getTitle());
        assertNotNull(stage.getScene());
        assertTrue(stage.getScene().getWidth() > 0);
        assertTrue(stage.getScene().getHeight() > 0);
    }

    private static void executeAppAction(ShoppingCartApp app, javafx.stage.Stage stage, boolean startApplication) {
        if (startApplication) {
            startApp(app, stage);
        } else {
            loadScene(app, stage);
        }
    }

    private static void loadScene(ShoppingCartApp app, javafx.stage.Stage stage) {
        try {
            app.loadScene(stage, ShoppingCartApp.ENGLISH);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load scene.", e);
        }
    }

    private static void startApp(ShoppingCartApp app, javafx.stage.Stage stage) {
        try {
            app.start(stage);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start application.", e);
        }
    }
}