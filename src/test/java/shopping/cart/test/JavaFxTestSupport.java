package shopping.cart.test;



import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;

import java.util.concurrent.CountDownLatch;

abstract class JavaFxTestSupport {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {
                // JavaFX initialized for tests
            });
        } catch (IllegalStateException ignored) {
            // already initialized
        }
    }

    protected static Stage createStage() {
        final Stage[] stageHolder = new Stage[1];
        runOnFxThreadAndWait(() -> stageHolder[0] = new Stage());
        return stageHolder[0];
    }

    protected static void runOnFxThreadAndWait(FxTestAction action) {
        CountDownLatch latch = new CountDownLatch(1);
        final RuntimeException[] error = new RuntimeException[1];

        Platform.runLater(() -> {
            try {
                action.run();
            } catch (RuntimeException e) {
                error[0] = e;
            } finally {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for JavaFX task completion.", e);
        }

        if (error[0] != null) {
            throw error[0];
        }
    }

    @FunctionalInterface
    protected interface FxTestAction {
        void run();
    }
}