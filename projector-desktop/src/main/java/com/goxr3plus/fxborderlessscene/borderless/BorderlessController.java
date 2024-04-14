package com.goxr3plus.fxborderlessscene.borderless;

import com.goxr3plus.fxborderlessscene.window.TransparentWindow;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller implements window controls: maximize, minimize, drag, and Aero Snap.
 *
 * @author Nicolas Senet-Larson
 * @author GOXR3PLUS STUDIO
 * @version 1.0
 */
public class BorderlessController {
    private static final Logger LOG = LoggerFactory.getLogger(BorderlessController.class);

    private final SimpleBooleanProperty maximized;
    private final SimpleBooleanProperty resizable;
    private final SimpleBooleanProperty snap;
    Delta prevSize;
    Delta prevPos;
    String bottom = "bottom";
    private Stage stage;
    private boolean snapped;
    @FXML
    private Pane leftPane;
    @FXML
    private Pane rightPane;
    @FXML
    private Pane topPane;
    @FXML
    private Pane bottomPane;
    @FXML
    private Pane topLeftPane;
    @FXML
    private Pane topRightPane;
    @FXML
    private Pane bottomLeftPane;
    @FXML
    private Pane bottomRightPane;
    /**
     * Transparent Window used to show how the window will be resized
     */
    private TransparentWindow transparentWindow;

    public BorderlessController() {
        prevSize = new Delta();
        prevPos = new Delta();
        maximized = new SimpleBooleanProperty(false);
        resizable = new SimpleBooleanProperty(true);
        snap = new SimpleBooleanProperty(true);
        snapped = false;
    }

    public static Screen getScreenFromStage(Stage stage) {
        double x = stage.getX();
        double y = stage.getY();
        double width = stage.getWidth();
        double height = stage.getHeight();
        ObservableList<Screen> screensForRectangle =
                Screen.getScreensForRectangle(x, y, width / 2, height / 2);
        if (screensForRectangle.isEmpty()) {
            screensForRectangle = Screen.getScreensForRectangle(x, y, width, height);
        }
        return screensForRectangle.get(0);
    }

    /**
     * Creates the Transparent Window
     *
     * @param parentWindow The parentWindow of the TransparentWindow
     */
    public void createTransparentWindow(Stage parentWindow) {
        transparentWindow = new TransparentWindow();
        transparentWindow.getWindow().initOwner(parentWindow);
    }

    /**
     * Called after the FXML layout is loaded.
     */
    @FXML
    private void initialize() {
        setResizeControl(leftPane, "left");
        setResizeControl(rightPane, "right");
        setResizeControl(topPane, "top");
        setResizeControl(bottomPane, bottom);
        setResizeControl(topLeftPane, "top-left");
        setResizeControl(topRightPane, "top-right");
        setResizeControl(bottomLeftPane, bottom + "-left");
        setResizeControl(bottomRightPane, bottom + "-right");

        BooleanBinding negateOfResizable = resizable.not();
        leftPane.disableProperty().bind(negateOfResizable);
        rightPane.disableProperty().bind(negateOfResizable);
        topPane.disableProperty().bind(negateOfResizable);
        bottomPane.disableProperty().bind(negateOfResizable);
        topLeftPane.disableProperty().bind(negateOfResizable);
        topRightPane.disableProperty().bind(negateOfResizable);
        bottomLeftPane.disableProperty().bind(negateOfResizable);
        bottomRightPane.disableProperty().bind(negateOfResizable);
    }

    /**
     * Set the Stage of the controller.
     *
     * @param primaryStage the new stage
     */
    protected void setStage(Stage primaryStage) {
        this.stage = primaryStage;
    }

    /**
     * Maximize on/off the application.
     */
    protected void maximize() {
        Rectangle2D screen;

        try {
            screen = getScreenFromStage(stage).getVisualBounds();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return;
        }

        if (maximized.get()) {
            if (prevSize.x != null) {
                stage.setWidth(prevSize.x);
            }
            if (prevSize.y != null) {
                stage.setHeight(prevSize.y);
            }
            stage.setX(prevPos.x);
            stage.setY(prevPos.y);
            setMaximized(false);
        } else {
            // Record position and size, and maximize.
            if (!snapped) {
                prevSize.x = stage.getWidth();
                prevSize.y = stage.getHeight();
                prevPos.x = stage.getX();
                prevPos.y = stage.getY();
            } else if (prevSize.x == null || prevSize.y == null || !screen.contains(prevPos.x, prevPos.y)) {
                if (prevSize.x == null || prevSize.x > screen.getWidth()) {
                    prevSize.x = screen.getWidth() - 20;
                }
                if (prevSize.y == null || prevSize.y > screen.getHeight()) {
                    prevSize.y = screen.getHeight() - 20;
                }

                prevPos.x = screen.getMinX() + (screen.getWidth() - prevSize.x) / 2;
                prevPos.y = screen.getMinY() + (screen.getHeight() - prevSize.y) / 2;
            }

            stage.setX(screen.getMinX());
            stage.setY(screen.getMinY());
            stage.setWidth(screen.getWidth());
            stage.setHeight(screen.getHeight());

            setMaximized(true);
        }
    }

    /**
     * Set a node that can be pressed and dragged to move the application around.
     *
     * @param node the node.
     */
    protected void setMoveControl(final Node node) {
        final Delta delta = new Delta();
        final Delta eventSource = new Delta();

        // Record drag deltas on press.
        node.setOnMousePressed(m -> {
            if (m.isPrimaryButtonDown()) {
                delta.x = m.getSceneX(); //getX()
                delta.y = m.getSceneY(); //getY()

                if (maximized.get() || snapped) {
                    if (prevSize.x != null) {
                        delta.x = prevSize.x * (m.getSceneX() / stage.getWidth());
                    }
                    if (prevSize.y != null) {
                        delta.y = prevSize.y * (m.getSceneY() / stage.getHeight());
                    }
                } else {
                    prevSize.x = stage.getWidth();
                    prevSize.y = stage.getHeight();
                    prevPos.x = stage.getX();
                    prevPos.y = stage.getY();
                }

                eventSource.x = m.getScreenX();
                eventSource.y = node.prefHeight(stage.getHeight());
            }
        });

        // Dragging moves the application around.
        node.setOnMouseDragged(mouseEvent -> {
            if (mouseEvent.isPrimaryButtonDown()) {

                // Move x axis.
                stage.setX(mouseEvent.getScreenX() - delta.x);

                if (snapped) {
                    if (mouseEvent.getScreenY() > eventSource.y) {
                        snapOff();
                    } else {
                        Rectangle2D screen = Screen.getScreensForRectangle(mouseEvent.getScreenX(), mouseEvent.getScreenY(), 1, 1).get(0).getVisualBounds();
                        stage.setHeight(screen.getHeight());
                    }
                } else {
                    // Move y axis.
                    stage.setY(mouseEvent.getScreenY() - delta.y);
                }

                // Aero Snap off.
                if (maximized.get()) {
                    if (prevSize.x != null) {
                        stage.setWidth(prevSize.x);
                    }
                    if (prevSize.y != null) {
                        stage.setHeight(prevSize.y);
                    }
                    setMaximized(false);
                }

                boolean toCloseWindow = false;
                if (!snap.get()) {
                    toCloseWindow = true;
                } else {
                    //--------------------------Check here for Transparent Window--------------------------
                    //Rectangle2D wholeScreen = Screen.getScreensForRectangle(mouseEvent.getScreenX(), mouseEvent.getScreenY(), 1, 1).get(0).getBounds()
                    ObservableList<Screen> screens = Screen.getScreensForRectangle(mouseEvent.getScreenX(), mouseEvent.getScreenY(), 1, 1);
                    if (screens.isEmpty()) {
                        return;
                    }
                    Rectangle2D screenRectangle = screens.get(0).getVisualBounds();

                    //----------TO BE ADDED IN FUTURE RELEASE , GAVE ME CANCER implementing them ..----------------

                    //				// Aero Snap Top Right Corner
                    //				if (mouseEvent.getScreenY() <= screenRectangle.getMinY() && mouseEvent.getScreenX() >= screenRectangle.getMaxX() - 1) {
                    //					double difference;
                    //
                    //					//Fix the positioning
                    //					if (wholeScreen.getMaxX() > screenRectangle.getMaxX())
                    //						difference = - ( wholeScreen.getWidth() - screenRectangle.getWidth() );
                    //					else
                    //						difference =  (wholeScreen.getWidth() - screenRectangle.getWidth()-15);
                    //
                    //					System.out.println(difference);
                    //
                    //					transparentWindow.getWindow().setX(wholeScreen.getWidth() / 2 + difference);
                    //					transparentWindow.getWindow().setY(screenRectangle.getMinY());
                    //					transparentWindow.getWindow().setWidth(screenRectangle.getWidth() / 2);
                    //					transparentWindow.getWindow().setHeight(screenRectangle.getHeight() / 2);
                    //
                    //					transparentWindow.show();
                    //				}
                    //
                    //				// Aero Snap Top Left Corner
                    //				else if (mouseEvent.getScreenY() <= screenRectangle.getMinY() && mouseEvent.getScreenX() <= screenRectangle.getMinX()) {
                    //
                    //					transparentWindow.getWindow().setX(screenRectangle.getMinX());
                    //					transparentWindow.getWindow().setY(screenRectangle.getMinY());
                    //					transparentWindow.getWindow().setWidth(screenRectangle.getWidth() / 2);
                    //					transparentWindow.getWindow().setHeight(screenRectangle.getHeight() / 2);
                    //
                    //					transparentWindow.show();
                    //				}
                    //
                    //				// Aero Snap Bottom Right Corner
                    //				else if (mouseEvent.getScreenY() >= screenRectangle.getMaxY() - 1 && mouseEvent.getScreenX() >= screenRectangle.getMaxY()) {
                    //
                    //					transparentWindow.getWindow().setX(wholeScreen.getWidth() / 2 - ( wholeScreen.getWidth() - screenRectangle.getWidth() ));
                    //					transparentWindow.getWindow().setY(wholeScreen.getHeight() / 2 - ( wholeScreen.getHeight() - screenRectangle.getHeight() ));
                    //					transparentWindow.getWindow().setWidth(wholeScreen.getWidth() / 2);
                    //					transparentWindow.getWindow().setHeight(wholeScreen.getHeight() / 2);
                    //
                    //					transparentWindow.show();
                    //				}
                    //
                    //				// Aero Snap Bottom Left Corner
                    //				else if (mouseEvent.getScreenY() >= screenRectangle.getMaxY() - 1 && mouseEvent.getScreenX() <= screenRectangle.getMinX()) {
                    //
                    //					transparentWindow.getWindow().setX(screenRectangle.getMinX());
                    //					transparentWindow.getWindow().setY(wholeScreen.getHeight() / 2 - ( wholeScreen.getHeight() - screenRectangle.getHeight() ));
                    //					transparentWindow.getWindow().setWidth(wholeScreen.getWidth() / 2);
                    //					transparentWindow.getWindow().setHeight(wholeScreen.getHeight() / 2);
                    //
                    //					transparentWindow.show();
                    //				}

                    // Aero Snap Left.
                    Stage transparentWindowWindow = transparentWindow.getWindow();
                    if (mouseEvent.getScreenX() <= screenRectangle.getMinX()) {
                        transparentWindowWindow.setY(screenRectangle.getMinY());
                        transparentWindowWindow.setHeight(screenRectangle.getHeight());

                        transparentWindowWindow.setX(screenRectangle.getMinX());
                        transparentWindowWindow.setWidth(Math.max(screenRectangle.getWidth() / 2, transparentWindowWindow.getMinWidth()));

                        transparentWindow.show();
                    }

                    // Aero Snap Right.
                    else if (mouseEvent.getScreenX() >= screenRectangle.getMaxX() - 1) {
                        transparentWindowWindow.setY(screenRectangle.getMinY());
                        transparentWindowWindow.setHeight(screenRectangle.getHeight());

                        transparentWindowWindow.setWidth(Math.max(screenRectangle.getWidth() / 2, transparentWindowWindow.getMinWidth()));
                        transparentWindowWindow.setX(screenRectangle.getMaxX() - transparentWindowWindow.getWidth());

                        transparentWindow.show();
                    }

                    // Aero Snap Top. || Aero Snap Bottom.
                    else if (mouseEvent.getScreenY() <= screenRectangle.getMinY() || mouseEvent.getScreenY() >= screenRectangle.getMaxY() - 1) {

                        transparentWindowWindow.setX(screenRectangle.getMinX());
                        transparentWindowWindow.setY(screenRectangle.getMinY());
                        transparentWindowWindow.setWidth(screenRectangle.getWidth());
                        transparentWindowWindow.setHeight(screenRectangle.getHeight());

                        transparentWindow.show();
                    } else {
                        toCloseWindow = true;
                    }
                }

                if (toCloseWindow) {
                    transparentWindow.close();
                }
            }
        });

        // Maximize on double click.
        node.setOnMouseClicked(m -> {
            if (snap.get() && (MouseButton.PRIMARY.equals(m.getButton())) && (m.getClickCount() == 2)) maximize();
        });

        // Aero Snap on release.
        node.setOnMouseReleased(m -> {

            try {
                if (!snap.get()) {
                    return;
                }
                if ((MouseButton.PRIMARY.equals(m.getButton())) && ((eventSource.x != null) && m.getScreenX() != eventSource.x)) {
                    Rectangle2D screen = Screen.getScreensForRectangle(m.getScreenX(), m.getScreenY(), 1, 1).get(0).getVisualBounds();

                    // Aero Snap Left.
                    if (m.getScreenX() <= screen.getMinX()) {
                        stage.setY(screen.getMinY());
                        stage.setHeight(screen.getHeight());

                        stage.setX(screen.getMinX());
                        stage.setWidth(Math.max(screen.getWidth() / 2, stage.getMinWidth()));

                        snapped = true;
                    }

                    // Aero Snap Right.
                    else if (m.getScreenX() >= screen.getMaxX() - 1) {
                        stage.setY(screen.getMinY());
                        stage.setHeight(screen.getHeight());

                        stage.setWidth(Math.max(screen.getWidth() / 2, stage.getMinWidth()));
                        stage.setX(screen.getMaxX() - stage.getWidth());

                        snapped = true;
                    }

                    // Aero Snap Top ||  Aero Snap Bottom
                    else if (m.getScreenY() <= screen.getMinY() || m.getScreenY() >= screen.getMaxY() - 1) {
                        if (prevSize.x == null || prevSize.y == null || !screen.contains(prevPos.x, prevPos.y)) {
                            if (prevSize.x == null || prevSize.x > screen.getWidth()) {
                                prevSize.x = screen.getWidth() - 20;
                            }
                            if (prevSize.y == null || prevSize.y > screen.getHeight()) {
                                prevSize.y = screen.getHeight() - 20;
                            }
                            prevPos.x = screen.getMinX() + (screen.getWidth() - prevSize.x) / 2;
                            prevPos.y = screen.getMinY() + (screen.getHeight() - prevSize.y) / 2;
                        }

                        stage.setX(screen.getMinX());
                        stage.setY(screen.getMinY());
                        stage.setWidth(screen.getWidth());
                        stage.setHeight(screen.getHeight());
                        setMaximized(true);
                    }

                    //				System.out.println("Mouse Position [ " + m.getScreenX() + "," + m.getScreenY() + " ]")
                    //				System.out.println(" " + screen.getMinX() + "," + screen.getMinY() + " ," + screen.getMaxX() + " ," + screen.getMaxY())
                    //				System.out.println()

                }

            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }

            //Hide the transparent window -- close this window no matter what
            transparentWindow.close();
        });
    }

    private void snapOff() {
        if (prevSize.x != null) {
            stage.setWidth(prevSize.x);
        }
        if (prevSize.y != null) {
            stage.setHeight(prevSize.y);
        }
        snapped = false;
    }

    /**
     * Set pane to resize application when pressed and dragged.
     *
     * @param pane      the pane the action is set to.
     * @param direction the resize direction. Diagonal: 'top' or 'bottom' + 'right' or 'left'. [[SuppressWarningsSpartan]]
     */
    private void setResizeControl(Pane pane, final String direction) {

        //Record the previous size and previous point
        pane.setOnDragDetected((event) -> {
            prevSize.x = stage.getWidth();
            prevSize.y = stage.getHeight();
            prevPos.x = stage.getX();
            prevPos.y = stage.getY();
        });

        pane.setOnMouseDragged(m -> {
            if (m.isPrimaryButtonDown()) {
                double width = stage.getWidth();
                double height = stage.getHeight();

                // Horizontal resize.
                if (direction.endsWith("left")) {
                    double comingWidth = width - m.getScreenX() + stage.getX();

                    //Check if it violates minimumWidth
                    if (comingWidth > 0 && comingWidth >= stage.getMinWidth()) {
                        stage.setWidth(stage.getX() - m.getScreenX() + stage.getWidth());
                        stage.setX(m.getScreenX());
                    }

                } else if (direction.endsWith("right")) {
                    double comingWidth = width + m.getX();

                    //Check if it violates
                    if (comingWidth > 0 && comingWidth >= stage.getMinWidth()) {
                        stage.setWidth(m.getSceneX());
                    }
                }

                // Vertical resize.
                if (direction.startsWith("top")) {
                    if (snapped) {
                        if (prevSize.y != null) {
                            stage.setHeight(prevSize.y);
                        }
                        snapped = false;
                    } else {
                        double newHeight = stage.getY() - m.getScreenY() + stage.getHeight();
                        if (checkNewHeight(newHeight, height)) {
                            stage.setHeight(newHeight);
                            stage.setY(m.getScreenY());
                        }
                    }
                } else if (direction.startsWith(bottom)) {
                    if (snapped) {
                        if (prevSize.y != null) {
                            stage.setY(prevPos.y);
                        }
                        snapped = false;
                    } else {
                        double newHeight = m.getSceneY();
                        //Check if it violates
                        if (checkNewHeight(newHeight, height)) {
                            stage.setHeight(newHeight);
                        }
                    }

                }
            }
        });

        // Record application height and y position.
        pane.setOnMousePressed(m -> {
            if ((m.isPrimaryButtonDown()) && (!snapped)) {
                prevSize.y = stage.getHeight();
                prevPos.y = stage.getY();
            }

        });

        // Aero Snap Resize.
        pane.setOnMouseReleased(m -> {
            if ((MouseButton.PRIMARY.equals(m.getButton())) && (!snapped)) {
                Rectangle2D screen = Screen.getScreensForRectangle(m.getScreenX(), m.getScreenY(), 1, 1).get(0).getVisualBounds();

                if ((stage.getY() <= screen.getMinY()) && (direction.startsWith("top"))) {
                    stage.setHeight(screen.getHeight());
                    stage.setY(screen.getMinY());
                    snapped = true;
                }

                if ((m.getScreenY() >= screen.getMaxY()) && (direction.startsWith(bottom))) {
                    stage.setHeight(screen.getHeight());
                    stage.setY(screen.getMinY());
                    snapped = true;
                }
            }

        });

        // Aero Snap resize on double click.
        pane.setOnMouseClicked(m -> {
            if ((MouseButton.PRIMARY.equals(m.getButton())) && (m.getClickCount() == 2) && ("top".equals(direction) || bottom.equals(direction))) {
                Rectangle2D screen = Screen.getScreensForRectangle(stage.getX(), stage.getY(), stage.getWidth() / 2, stage.getHeight() / 2).get(0).getVisualBounds();

                if (snapped) {
                    stage.setHeight(prevSize.y);
                    stage.setY(prevPos.y);
                    snapped = false;
                } else {
                    prevSize.y = stage.getHeight();
                    prevPos.y = stage.getY();
                    stage.setHeight(screen.getHeight());
                    stage.setY(screen.getMinY());
                    snapped = true;
                }
            }

        });
    }

    private boolean checkNewHeight(double newHeight, double height) {
        return newHeight > 0 && (newHeight >= stage.getMinHeight() || newHeight > height);
    }

    /**
     * Determines if the Window is maximized.
     *
     * @param maximized the new maximized
     */
    private void setMaximized(boolean maximized) {
        this.maximized.set(maximized);
        setResizable(!maximized);
    }

    /**
     * Disable/enable the resizing of your stage. Enabled by default.
     *
     * @param bool false to disable, true to enable.
     */
    protected void setResizable(boolean bool) {
        resizable.set(bool);
    }

    /**
     * Disable/enable the Aero Snap property of your stage. Enabled by default.
     *
     * @param bool false to disable, true to enable.
     */
    protected void setSnapEnabled(boolean bool) {
        snap.set(bool);
        if (!bool && snapped) {
            snapOff();
        }
    }

    public SimpleBooleanProperty maximizedProperty() {
        return maximized;
    }

    public void ensureMaximizeStage(boolean maximized) {
        if (isMaximized() != maximized) {
            maximize();
        }
    }

    private boolean isMaximized() {
        return maximized.get();
    }
}
