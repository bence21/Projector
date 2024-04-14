package com.goxr3plus.fxborderlessscene.borderless;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

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
public class BorderlessPane extends AnchorPane {
    public BorderlessPane(BorderlessController controller) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Borderless.fxml"));
        loader.setController(controller);
        loader.setRoot(this);
        loader.load();
    }
}
