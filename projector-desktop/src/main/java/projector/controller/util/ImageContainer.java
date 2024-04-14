package projector.controller.util;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ImageContainer {
    private final Label filenameLabel;
    private final Rectangle highlightRect;
    private final BorderPane container;
    private StackPane mainPane;
    private String fileImagePath;

    public ImageContainer(Label filenameLabel, BorderPane container) {
        this.filenameLabel = filenameLabel;
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

    public Label getFilenameLabel() {
        return filenameLabel;
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

    // public void setImage(Image image) {
    //     this.image = image;
    // }

    public String getFileImagePath() {
        return fileImagePath;
    }

    public void setFileImagePath(String fileImagePath) {
        this.fileImagePath = fileImagePath;
    }
}
