package com.goxr3plus.fxborderlessscene.borderless;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Undecorated JavaFX Scene with implemented move, resize, minimize, maximize and Aero Snap.
 * <p>
 * Usage:
 *
 * <pre>
 * {
 * 	&#64;code
 *     //add the code here
 * }
 * </pre>
 *
 * @author Nicolas Senet-Larson
 * @author GOXR3PLUS STUDIO
 * @version 1.0
 */
public class BorderlessScene extends Scene {
    private static final Logger LOG = LoggerFactory.getLogger(BorderlessScene.class);

    /**
     * The controller.
     */
    private BorderlessController controller;

    /**
     * The root.
     */
    private BorderlessPane root;

    /**
     * The constructor.
     *
     * @param stage     your stage.
     * @param sceneRoot The root of the Scene
     */
    public BorderlessScene(Stage stage, Parent sceneRoot) {
        super(new Pane());
        try {
            this.controller = new BorderlessController();
            // Load the FXML
            this.root = new BorderlessPane(this.controller);

            // Set Scene root
            setRoot(this.root);
            setContent(sceneRoot);

            // Initialize the Controller
            this.controller.setStage(stage);
            this.controller.createTransparentWindow(stage);

            // StageStyle
            StageStyle stageStyle = stage.getStyle();
            if (stageStyle == StageStyle.UTILITY) {
                setSnapEnabled(false);
                setResizable(false);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Change the content of the scene.
     *
     * @param content the root Parent of your new content.
     */
    public void setContent(Parent content) {
        this.root.getChildren().remove(0);
        this.root.getChildren().add(0, content);
        AnchorPane.setLeftAnchor(content, 0.0D);
        AnchorPane.setTopAnchor(content, 0.0D);
        AnchorPane.setRightAnchor(content, 0.0D);
        AnchorPane.setBottomAnchor(content, 0.0D);
    }

    /**
     * Set a node that can be pressed and dragged to move the application around.
     *
     * @param node the node.
     */
    public void setMoveControl(Node node) {
        this.controller.setMoveControl(node);
    }

    /**
     * Toggle to maximize the application.
     */
    public void maximizeStage() {
        controller.maximize();
    }

    /**
     * Disable/enable the resizing of your stage. Enabled by default.
     *
     * @param bool false to disable, true to enable.
     */
    public void setResizable(boolean bool) {
        controller.setResizable(bool);
    }

    /**
     * Disable/enable the Aero Snap of your stage. Enabled by default.
     *
     * @param bool false to disable, true to enable.
     */
    public void setSnapEnabled(boolean bool) {
        controller.setSnapEnabled(bool);
    }

    public BorderlessController getController() {
        return controller;
    }

    public void ensureMaximizeStage(boolean maximized) {
        controller.ensureMaximizeStage(maximized);
    }
}
