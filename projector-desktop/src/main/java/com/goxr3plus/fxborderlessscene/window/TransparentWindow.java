package com.goxr3plus.fxborderlessscene.window;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author GOXR3PLUS
 */
public class TransparentWindow extends StackPane {

    //--------------------------------------------------------

    /**
     * The Window
     */
    private final Stage window = new Stage();

    //--------------------------------------------------------
    @FXML
    private StackPane stackPane;

    /**
     * Constructor
     */
    public TransparentWindow() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TransparentWindow.fxml"));
        loader.setController(this);
        loader.setRoot(this);

        try {
            loader.load();
        } catch (IOException ex) {
            Logger logger = Logger.getLogger(getClass().getName());
            logger.log(Level.SEVERE, "", ex);
        }

        //Window
        window.setTitle("Transparent Window");
        window.initStyle(StageStyle.TRANSPARENT);
        window.initModality(Modality.NONE);
        window.setScene(new Scene(this, Color.TRANSPARENT));
    }

    /**
     * @return the window
     */
    public Stage getWindow() {
        return window;
    }

    /**
     * Close the Window
     */
    public void close() {
        window.close();
    }

    /**
     * Show the Window
     */
    public void show() {
        if (!window.isShowing())
            window.show();
        else
            window.requestFocus();
    }

}
