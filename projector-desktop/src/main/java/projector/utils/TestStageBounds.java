package projector.utils;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import projector.application.ApplicationVersion;

public final class TestStageBounds {

    private TestStageBounds() {
    }

    public static boolean shouldCap() {
        return ApplicationVersion.getInstance().isTesting()
                && !ApplicationVersion.getInstance().isAllowUncappedTestWindowSize();
    }

    public static Rectangle2D getPrimaryVisualBounds() {
        return Screen.getPrimary().getVisualBounds();
    }

    public static double cappedWidth(double width) {
        if (!shouldCap()) {
            return width;
        }
        return Math.min(width, getPrimaryVisualBounds().getWidth());
    }

    public static double cappedHeight(double height) {
        if (!shouldCap()) {
            return height;
        }
        return Math.min(height, getPrimaryVisualBounds().getHeight());
    }

    public static void positionStage(Stage stage) {
        if (!shouldCap()) {
            stage.setX(0);
            stage.setY(0);
            return;
        }
        Rectangle2D bounds = getPrimaryVisualBounds();
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
    }
}
