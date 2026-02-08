package projector.controller.util;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ImageContainer {
    private final Rectangle highlightRect;
    private final BorderPane container;
    private StackPane mainPane;
    private String fileImagePath;
    private boolean isFolder = false;

    public ImageContainer(BorderPane container) {
        this.container = container;
        this.highlightRect = createHighlightRect();
        container.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                highlightRect.setWidth(newValue.doubleValue());
            }
        });
        container.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                highlightRect.setHeight(newValue.doubleValue());
            }
        });
    }

    private Rectangle createHighlightRect() {
        Rectangle rect = new Rectangle(200, 200);
        rect.setFill(Color.rgb(255, 255, 255, 0.21));
        rect.setVisible(false);
        return rect;
    }

    public BorderPane getContainer() {
        return container;
    }

    public Rectangle getHighlightRect() {
        return highlightRect;
    }

    public StackPane getMainPane() {
        return mainPane;
    }

    public void setMainPane(StackPane mainPane) {
        this.mainPane = mainPane;
    }

    public String getFileImagePath() {
        return fileImagePath;
    }

    public void setFileImagePath(String fileImagePath) {
        this.fileImagePath = fileImagePath;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setFolder(boolean folder) {
        isFolder = folder;
    }

}
