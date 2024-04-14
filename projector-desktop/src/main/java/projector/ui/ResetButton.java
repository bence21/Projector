package projector.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.TextAlignment;
import projector.application.Settings;

import java.io.InputStream;

import static java.lang.Double.NEGATIVE_INFINITY;

public class ResetButton extends Button {

    public ResetButton() {
        setContentDisplay(ContentDisplay.CENTER);
        setGraphicTextGap(0.0);
        setMaxHeight(NEGATIVE_INFINITY);
        setMaxWidth(NEGATIVE_INFINITY);
        setMnemonicParsing(false);
        double prefSize = 17.0;
        setPrefHeight(prefSize);
        setPrefWidth(prefSize);
        setTextAlignment(TextAlignment.CENTER);
        InputStream resourceAsStream = getClass().getResourceAsStream("/icons/reset.png");
        if (resourceAsStream != null) {
            ImageView imageView = new ImageView(new Image(resourceAsStream));
            double fitSize = 16.0;
            imageView.setFitHeight(fitSize);
            imageView.setFitWidth(fitSize);
            imageView.setPickOnBounds(true);
            imageView.setPreserveRatio(true);
            setGraphic(imageView);
        }
        String reset_to_default_setting = Settings.getInstance().getResourceBundle().getString("Reset to default setting");
        setTooltip(new Tooltip(reset_to_default_setting));
        setPadding(new Insets(2.0));
    }


    @Override
    protected void setWidth(double value) {
        super.setWidth(value);
    }

    @Override
    protected void setHeight(double value) {
        super.setHeight(value);
    }

    @Override
    public void setMinSize(double minWidth, double minHeight) {
        super.setMinSize(minWidth, minHeight);
    }

    @Override
    public void setPrefSize(double prefWidth, double prefHeight) {
        super.setPrefSize(prefWidth, prefHeight);
    }

    @Override
    public void setMaxSize(double maxWidth, double maxHeight) {
        super.setMaxSize(maxWidth, maxHeight);
    }

    public void initialize() {
        System.out.println("initialized");
    }


    public void setOnAction2(EventHandler<ActionEvent> value) {
        setOnAction(event -> {
            value.handle(event);
            setVisible(false);
        });
    }
}
