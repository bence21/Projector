package projector.controller;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.controller.util.ProjectionScreensUtil;

public class BrowserController {

    private static final Logger LOG = LoggerFactory.getLogger(BrowserController.class);
    private static final String DEFAULT_URL = "https://www.google.com";
    private static final int MIN_SELECTION_SIZE = 10;

    private final ProjectionScreensUtil projectionScreensUtil = ProjectionScreensUtil.getInstance();

    @FXML
    private TextField urlTextField;
    @FXML
    private ToggleButton selectRegionToggle;
    @FXML
    private ToggleButton selectImageToggle;
    @FXML
    private WebView webView;
    @FXML
    private StackPane webViewStackPane;

    private Pane regionOverlay;
    private Rectangle selectionRectangle;
    private double selectionStartX;
    private double selectionStartY;
    private boolean selectionInProgress;
    private boolean initialized;

    public void onTabOpened() {
        if (initialized) {
            return;
        }
        initialized = true;
        if (urlTextField.getText() == null || urlTextField.getText().isBlank()) {
            urlTextField.setText(DEFAULT_URL);
            loadUrl(DEFAULT_URL);
        }
        urlTextField.setOnAction(event -> onGo());
        setupSelectImageMode();
    }

    private void setupSelectImageMode() {
        // Use event filters so we run in capture phase and consume before the WebView passes the event to the page
        webView.addEventFilter(MouseEvent.MOUSE_PRESSED, this::filterMouseEventOnImage);
        webView.addEventFilter(MouseEvent.MOUSE_CLICKED, this::filterMouseEventOnImage);
    }

    private void filterMouseEventOnImage(MouseEvent event) {
        if (!selectImageToggle.isSelected() || event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        if (selectRegionToggle.isSelected()) {
            return;
        }
        double x = event.getX();
        double y = event.getY();
        try {
            // Get bounding rect of the img element (viewport coordinates) so we can capture it from the WebView snapshot.
            // Wrapped in IIFE so return is valid (executeScript runs top-level code, not a function body).
            String script = "(function(){ var el = document.elementFromPoint(" + (int) x + "," + (int) y + "); " +
                    "if (el && el.tagName === 'IMG') { var r = el.getBoundingClientRect(); " +
                    "return r.left + ',' + r.top + ',' + r.width + ',' + r.height; } return null; })()";
            Object result = webView.getEngine().executeScript(script);
            if (result instanceof String rectStr && !rectStr.isBlank()) {
                event.consume();
                if (event.getEventType() == MouseEvent.MOUSE_CLICKED) {
                    projectImageFromWebViewCapture(rectStr);
                }
            }
        } catch (Exception e) {
            LOG.debug("No image at click or script failed: {}", e.getMessage());
        }
    }

    /**
     * Capture the image from the WebView by cropping the snapshot to the element's rect.
     * This avoids loading the URL again (which often fails due to CORS, redirects, or server blocking).
     */
    private void projectImageFromWebViewCapture(String rectStr) {
        try {
            String[] parts = rectStr.split(",");
            if (parts.length != 4) return;
            double left = Double.parseDouble(parts[0].trim());
            double top = Double.parseDouble(parts[1].trim());
            double width = Double.parseDouble(parts[2].trim());
            double height = Double.parseDouble(parts[3].trim());
            if (width < 1 || height < 1) return;

            int fullW = Math.max(1, (int) webView.getWidth());
            int fullH = Math.max(1, (int) webView.getHeight());
            WritableImage fullImage = new WritableImage(fullW, fullH);
            webView.snapshot(new SnapshotParameters(), fullImage);

            int ix = (int) Math.max(0, Math.min(left, fullW - 1));
            int iy = (int) Math.max(0, Math.min(top, fullH - 1));
            int iw = (int) Math.max(1, Math.min(width, fullW - ix));
            int ih = (int) Math.max(1, Math.min(height, fullH - iy));

            PixelReader reader = fullImage.getPixelReader();
            WritableImage cropped = new WritableImage(reader, ix, iy, iw, ih);
            projectionScreensUtil.drawImage(cropped);
        } catch (Exception e) {
            LOG.warn("Failed to capture image from page: {}", e.getMessage());
        }
    }

    @FXML
    private void onGo() {
        String url = urlTextField.getText();
        if (url == null || url.isBlank()) {
            return;
        }
        loadUrl(normalizeUrl(url));
    }

    private void loadUrl(String url) {
        try {
            webView.getEngine().load(url);
        } catch (Exception e) {
            LOG.error("Failed to load URL: {}", url, e);
        }
    }

    private static String normalizeUrl(String input) {
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        String lower = trimmed.toLowerCase();
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            return "https://" + trimmed;
        }
        return trimmed;
    }

    @FXML
    private void onCastFullPage() {
        Worker.State state = webView.getEngine().getLoadWorker().getState();
        if (state == Worker.State.SUCCEEDED) {
            captureAndProjectFullPage();
        } else {
            webView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    Platform.runLater(this::captureAndProjectFullPage);
                }
            });
        }
    }

    private void captureAndProjectFullPage() {
        Platform.runLater(() -> {
            try {
                SnapshotParameters params = new SnapshotParameters();
                int w = Math.max(1, (int) webView.getWidth());
                int h = Math.max(1, (int) webView.getHeight());
                WritableImage image = new WritableImage(w, h);
                webView.snapshot(params, image);
                projectionScreensUtil.drawImage(image);
            } catch (Exception e) {
                LOG.error("Failed to capture and project full page", e);
            }
        });
    }

    @FXML
    private void onSelectRegionToggle() {
        if (selectRegionToggle.isSelected()) {
            selectImageToggle.setSelected(false);
            showRegionOverlay();
        } else {
            hideRegionOverlay();
        }
    }

    @FXML
    private void onSelectImageToggle() {
        if (selectImageToggle.isSelected()) {
            selectRegionToggle.setSelected(false);
            hideRegionOverlay();
        }
    }

    private void showRegionOverlay() {
        if (regionOverlay != null) {
            if (!webViewStackPane.getChildren().contains(regionOverlay)) {
                webViewStackPane.getChildren().add(regionOverlay);
            }
            return;
        }

        regionOverlay = new Pane();
        regionOverlay.setPickOnBounds(true);
        regionOverlay.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));

        Rectangle dimRectangle = new Rectangle();
        dimRectangle.setFill(Color.rgb(0, 0, 0, 0.5));
        dimRectangle.setMouseTransparent(false);

        selectionRectangle = new Rectangle();
        selectionRectangle.setFill(Color.TRANSPARENT);
        selectionRectangle.setStroke(Color.WHITE);
        selectionRectangle.setStrokeWidth(2);
        selectionRectangle.setMouseTransparent(true);

        regionOverlay.getChildren().addAll(dimRectangle, selectionRectangle);

        regionOverlay.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                selectionStartX = event.getX();
                selectionStartY = event.getY();
                selectionRectangle.setX(selectionStartX);
                selectionRectangle.setY(selectionStartY);
                selectionRectangle.setWidth(0);
                selectionRectangle.setHeight(0);
                selectionInProgress = true;
            }
        });

        regionOverlay.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.PRIMARY && selectionInProgress) {
                double x = Math.min(selectionStartX, event.getX());
                double y = Math.min(selectionStartY, event.getY());
                double w = Math.abs(event.getX() - selectionStartX);
                double h = Math.abs(event.getY() - selectionStartY);
                selectionRectangle.setX(x);
                selectionRectangle.setY(y);
                selectionRectangle.setWidth(w);
                selectionRectangle.setHeight(h);
            }
        });

        regionOverlay.setOnMouseReleased(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                selectionInProgress = false;
            }
        });

        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10));

        Button projectSelectionButton = new Button("Project selection");
        projectSelectionButton.setOnAction(e -> projectSelection());

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> {
            selectRegionToggle.setSelected(false);
            hideRegionOverlay();
        });

        buttonBox.getChildren().addAll(projectSelectionButton, cancelButton);

        dimRectangle.widthProperty().bind(regionOverlay.widthProperty());
        dimRectangle.heightProperty().bind(regionOverlay.heightProperty());

        regionOverlay.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
            buttonBox.setLayoutX((newVal.getWidth() - 220) / 2);
            buttonBox.setLayoutY(Math.max(0, newVal.getHeight() - 50));
        });

        regionOverlay.getChildren().add(buttonBox);

        webViewStackPane.getChildren().add(regionOverlay);
        regionOverlay.prefWidthProperty().bind(webViewStackPane.widthProperty());
        regionOverlay.prefHeightProperty().bind(webViewStackPane.heightProperty());
        regionOverlay.setMinSize(0, 0);
    }

    private void hideRegionOverlay() {
        if (regionOverlay != null && regionOverlay.getParent() != null) {
            webViewStackPane.getChildren().remove(regionOverlay);
        }
    }

    private void projectSelection() {
        double x = selectionRectangle.getX();
        double y = selectionRectangle.getY();
        double w = selectionRectangle.getWidth();
        double h = selectionRectangle.getHeight();

        if (w < MIN_SELECTION_SIZE || h < MIN_SELECTION_SIZE) {
            LOG.warn("Selection too small");
            return;
        }

        Platform.runLater(() -> {
            try {
                SnapshotParameters params = new SnapshotParameters();
                int fullW = Math.max(1, (int) webView.getWidth());
                int fullH = Math.max(1, (int) webView.getHeight());
                WritableImage fullImage = new WritableImage(fullW, fullH);
                webView.snapshot(params, fullImage);

                int ix = (int) Math.max(0, Math.min(x, fullW - 1));
                int iy = (int) Math.max(0, Math.min(y, fullH - 1));
                int iw = (int) Math.max(1, Math.min(w, fullW - ix));
                int ih = (int) Math.max(1, Math.min(h, fullH - iy));

                PixelReader reader = fullImage.getPixelReader();
                WritableImage cropped = new WritableImage(reader, ix, iy, iw, ih);
                projectionScreensUtil.drawImage(cropped);
                selectRegionToggle.setSelected(false);
                hideRegionOverlay();
            } catch (Exception e) {
                LOG.error("Failed to project selection", e);
            }
        });
    }
}
