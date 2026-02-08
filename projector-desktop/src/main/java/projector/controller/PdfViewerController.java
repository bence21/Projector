package projector.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.ProjectionType;
import projector.application.Settings;
import projector.controller.util.PdfService;
import projector.controller.util.ProjectionScreensUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static projector.controller.ProjectionScreenController.drawMiddle;

public class PdfViewerController {

    private static final Logger LOG = LoggerFactory.getLogger(PdfViewerController.class);
    private static final int THUMBNAIL_SIZE = 200;
    private static final float THUMBNAIL_DPI = 72.0f;

    @FXML
    private BorderPane borderPane;
    @FXML
    private HBox navigationHBox;
    @FXML
    private Button previousButton;
    @FXML
    private Button nextButton;
    @FXML
    private Label pageCounterLabel;
    @FXML
    private TextField pageNumberTextField;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private FlowPane flowPane;

    private String filePath;
    private int pageCount;
    private int currentPage = PdfService.NO_PAGE_SET;
    private boolean shouldAutoSelectPage = false;
    private List<PageContainer> pageContainers;
    private PageContainer selectedPageContainer;
    private final ProjectionScreensUtil projectionScreensUtil = ProjectionScreensUtil.getInstance();
    private ExecutorService executorService;
    private volatile boolean isClosed = false;

    public void initialize() {
        pageContainers = new ArrayList<>();
        executorService = Executors.newFixedThreadPool(2);
        flowPane.setHgap(10);
        flowPane.setVgap(10);
        flowPane.setPadding(new Insets(10));

        setupKeyboardNavigation();
        setupPageNumberTextField();

        // Initialize text field state
        if (pageNumberTextField != null) {
            pageNumberTextField.setDisable(true);
        }
    }

    private void setupKeyboardNavigation() {
        if (borderPane == null) {
            return;
        }
        borderPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.PAGE_UP) {
                onPreviousPage();
                event.consume();
            } else if (event.getCode() == KeyCode.PAGE_DOWN) {
                onNextPage();
                event.consume();
            } else if (event.getCode() == KeyCode.LEFT) {
                navigateLeft();
                event.consume();
            } else if (event.getCode() == KeyCode.RIGHT) {
                navigateRight();
                event.consume();
            } else if (event.getCode() == KeyCode.UP) {
                navigateUp();
                event.consume();
            } else if (event.getCode() == KeyCode.DOWN) {
                navigateDown();
                event.consume();
            }
        });
    }

    private void setupPageNumberTextField() {
        if (pageNumberTextField == null) {
            return;
        }

        // Only allow numeric input
        pageNumberTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (!newValue.matches("\\d*")) {
                    pageNumberTextField.setText(oldValue);
                }
            }
        });

        // Handle Enter key to navigate
        pageNumberTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handlePageNumberInput();
            }
        });

        // Select all text when focused for easy editing
        pageNumberTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                Platform.runLater(() -> pageNumberTextField.selectAll());
            }
        });
    }

    private void handlePageNumberInput() {
        if (pageNumberTextField == null || pageCount == 0) {
            return;
        }

        String text = pageNumberTextField.getText().trim();
        if (text.isEmpty()) {
            updatePageNumberTextField();
            return;
        }

        try {
            int pageNumber = Integer.parseInt(text);
            goToPdfPage(pageNumber);
        } catch (NumberFormatException e) {
            // Invalid input, reset to current page
            updatePageNumberTextField();
        }
    }

    /**
     * Calculate how many columns fit in a row based on FlowPane width and item size.
     *
     * @return number of columns per row, or 1 if calculation cannot be performed
     */
    private int calculateColumnsPerRow() {
        if (flowPane == null || pageCount == 0) {
            return 1;
        }

        double flowPaneWidth = flowPane.getWidth();
        if (flowPaneWidth <= 0) {
            // If width is not yet calculated, use bounds
            flowPaneWidth = flowPane.getBoundsInLocal().getWidth();
        }

        if (flowPaneWidth <= 0) {
            return 1;
        }

        // Account for padding (10px on each side = 20px total)
        double availableWidth = flowPaneWidth - 20;

        // Item width includes thumbnail size plus horizontal gap
        double itemWidth = THUMBNAIL_SIZE + flowPane.getHgap();

        // Calculate how many items fit
        int columns = (int) Math.floor(availableWidth / itemWidth);
        return Math.max(1, columns);
    }

    /**
     * Calculate the row and column position of a page in the grid.
     *
     * @param pageIndex the page index (0-based)
     * @return a record containing row and column, or null if invalid
     */
    private GridPosition getGridPosition(int pageIndex) {
        if (pageIndex < 0 || pageIndex >= pageCount) {
            return null;
        }

        int columnsPerRow = calculateColumnsPerRow();
        if (columnsPerRow <= 0) {
            return null;
        }

        int row = pageIndex / columnsPerRow;
        int col = pageIndex % columnsPerRow;
        return new GridPosition(row, col);
    }

    /**
     * Convert grid coordinates to page index.
     *
     * @param row the row index (0-based)
     * @param col the column index (0-based)
     * @return the page index, or -1 if invalid
     */
    private int getPageIndexFromGrid(int row, int col) {
        int columnsPerRow = calculateColumnsPerRow();
        if (columnsPerRow <= 0) {
            return -1;
        }

        int pageIndex = row * columnsPerRow + col;
        if (pageIndex < 0 || pageIndex >= pageCount) {
            return -1;
        }

        return pageIndex;
    }

    /**
     * Record to hold grid position (row and column).
     */
    private record GridPosition(int row, int col) {
    }

    /**
     * Navigate to the previous column, wrapping to the end of the previous row if at the start.
     */
    private void navigateLeft() {
        if (currentPage < 0 || currentPage >= pageCount) {
            return;
        }

        GridPosition currentPos = getGridPosition(currentPage);
        if (currentPos == null) {
            return;
        }

        int columnsPerRow = calculateColumnsPerRow();
        int newCol = currentPos.col() - 1;
        int newRow = currentPos.row();

        // If at start of row, wrap to end of previous row
        if (newCol < 0) {
            if (newRow > 0) {
                newRow--;
                // Calculate how many items are in the previous row
                int itemsInPreviousRow = Math.min(columnsPerRow, pageCount - (newRow * columnsPerRow));
                newCol = itemsInPreviousRow - 1;
            } else {
                // Already at first item, don't navigate
                return;
            }
        }

        int newPageIndex = getPageIndexFromGrid(newRow, newCol);
        if (newPageIndex >= 0 && newPageIndex < pageCount) {
            goToPage(newPageIndex);
        }
    }

    /**
     * Navigate to the next column, wrapping to the start of the next row if at the end.
     */
    private void navigateRight() {
        if (currentPage < 0 || currentPage >= pageCount) {
            return;
        }

        GridPosition currentPos = getGridPosition(currentPage);
        if (currentPos == null) {
            return;
        }

        int columnsPerRow = calculateColumnsPerRow();
        int newCol = currentPos.col() + 1;
        int newRow = currentPos.row();

        // Check if we're at the end of the current row
        int itemsInCurrentRow = Math.min(columnsPerRow, pageCount - (newRow * columnsPerRow));
        if (newCol >= itemsInCurrentRow) {
            // Wrap to start of next row
            newRow++;
            newCol = 0;
        }

        int newPageIndex = getPageIndexFromGrid(newRow, newCol);
        if (newPageIndex >= 0 && newPageIndex < pageCount) {
            goToPage(newPageIndex);
        }
    }

    /**
     * Navigate to the same column in the previous row, stopping if at the top row.
     * If the target row has fewer columns, navigate to the last column of that row.
     */
    private void navigateUp() {
        if (currentPage < 0 || currentPage >= pageCount) {
            return;
        }

        GridPosition currentPos = getGridPosition(currentPage);
        if (currentPos == null || currentPos.row() <= 0) {
            // Already at top row, don't navigate
            return;
        }

        int columnsPerRow = calculateColumnsPerRow();
        int newRow = currentPos.row() - 1;
        int newCol = currentPos.col();

        // Check if target row has fewer items than the current column
        int itemsInTargetRow = Math.min(columnsPerRow, pageCount - (newRow * columnsPerRow));
        if (newCol >= itemsInTargetRow) {
            // Target row has fewer items, go to the last item in that row
            newCol = itemsInTargetRow - 1;
        }

        int newPageIndex = getPageIndexFromGrid(newRow, newCol);
        if (newPageIndex >= 0 && newPageIndex < pageCount) {
            goToPage(newPageIndex);
        }
    }

    /**
     * Navigate to the same column in the next row, stopping if at the bottom row.
     * If the target row has fewer columns, navigate to the last column of that row.
     */
    private void navigateDown() {
        if (currentPage < 0 || currentPage >= pageCount) {
            return;
        }

        GridPosition currentPos = getGridPosition(currentPage);
        if (currentPos == null) {
            return;
        }

        int columnsPerRow = calculateColumnsPerRow();
        int totalRows = (int) Math.ceil((double) pageCount / columnsPerRow);

        if (currentPos.row() >= totalRows - 1) {
            // Already at bottom row, don't navigate
            return;
        }

        int newRow = currentPos.row() + 1;
        int newCol = currentPos.col();

        // Check if target row has fewer items than the current column
        int itemsInTargetRow = Math.min(columnsPerRow, pageCount - (newRow * columnsPerRow));
        if (newCol >= itemsInTargetRow) {
            // Target row has fewer items, go to the last item in that row
            newCol = itemsInTargetRow - 1;
        }

        int newPageIndex = getPageIndexFromGrid(newRow, newCol);
        if (newPageIndex >= 0 && newPageIndex < pageCount) {
            goToPage(newPageIndex);
        }
    }

    public void setPdfFile(String filePath) {
        this.filePath = filePath;
        PdfService pdfService = PdfService.getInstance();
        pageCount = pdfService.getPageCount(filePath);
        currentPage = pdfService.getCurrentPage(filePath);
        // Only auto-select if the page was explicitly set (not just defaulting to 0)
        shouldAutoSelectPage = pdfService.hasCurrentPage(filePath);

        if (pageCount > 0) {
            loadPages();
            updateNavigation();
        }
    }

    private void loadPages() {
        flowPane.getChildren().clear();
        pageContainers.clear();

        for (int i = 0; i < pageCount; i++) {
            createPageThumbnail(i);
        }
    }

    private void createPageThumbnail(int pageIndex) {
        Canvas canvas = new Canvas(THUMBNAIL_SIZE, THUMBNAIL_SIZE);
        renderPageThumbnailAsync(pageIndex, canvas);

        PageUIComponents uiComponents = createPageUI(canvas, pageIndex);
        PageContainer pageContainer = new PageContainer(
                uiComponents.pageBorderPane
        );

        StackPane stackPane = createPageStackPane(pageContainer, uiComponents.pageBorderPane, pageIndex);
        pageContainer.setMainPane(stackPane);

        pageContainers.add(pageContainer);
        flowPane.getChildren().add(stackPane);
    }

    private void renderPageThumbnailAsync(int pageIndex, Canvas canvas) {
        // Don't submit new tasks if controller is already closed
        if (isClosed || executorService == null || executorService.isShutdown()) {
            return;
        }

        executorService.submit(() -> {
            try {
                // Check if controller was closed or thread was interrupted before starting
                if (isClosed || Thread.currentThread().isInterrupted()) {
                    return;
                }
                Image pageImage = PdfService.getInstance().renderPageAsImage(filePath, pageIndex, THUMBNAIL_DPI);
                if (pageImage != null && !isClosed) {
                    Platform.runLater(() -> {
                        // Check again before updating UI (tab might have been closed)
                        if (!isClosed && filePath != null && !Thread.currentThread().isInterrupted()) {
                            drawImageOnCanvas(pageImage, canvas);
                            // Only auto-select if we should (page was explicitly set) and it matches current page
                            if (shouldAutoSelectPage && pageIndex == currentPage) {
                                selectPage(pageIndex);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                // Only log if it's not an interruption-related exception
                // ClosedByInterruptException happens when tab is closed during loading - this is expected
                if (!isClosed && !(e.getCause() instanceof java.nio.channels.ClosedByInterruptException)) {
                    LOG.error("Error rendering PDF page {}: {}", pageIndex, e.getMessage(), e);
                }
            }
        });
    }

    private record PageUIComponents(Label pageLabel, BorderPane pageBorderPane) {
    }

    private PageUIComponents createPageUI(Canvas canvas, int pageIndex) {
        String pageText = Settings.getInstance().getResourceBundle().getString("Page") + " " + (pageIndex + 1);
        Label pageLabel = new Label(pageText);
        BorderPane pageBorderPane = new BorderPane();
        pageBorderPane.setCenter(canvas);
        pageBorderPane.setBottom(pageLabel);
        BorderPane.setAlignment(pageLabel, javafx.geometry.Pos.CENTER);
        BorderPane.setMargin(canvas, new Insets(5));
        return new PageUIComponents(pageLabel, pageBorderPane);
    }

    private StackPane createPageStackPane(PageContainer pageContainer, BorderPane pageBorderPane, int pageIndex) {
        Rectangle highlightRect = pageContainer.getHighlightRect();
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(highlightRect, pageBorderPane);
        stackPane.setOnMouseClicked(event -> onPageClicked(pageIndex));
        stackPane.setFocusTraversable(true);
        return stackPane;
    }

    private void drawImageOnCanvas(Image image, Canvas canvas) {
        if (image == null || canvas == null) {
            return;
        }
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawMiddle(image, canvas, gc);
    }

    private void onPageClicked(int pageIndex) {
        goToPage(pageIndex);
    }

    @FXML
    public void onPreviousPage() {
        if (currentPage > 0) {
            goToPage(currentPage - 1);
        }
    }

    @FXML
    public void onNextPage() {
        if (currentPage < pageCount - 1) {
            goToPage(currentPage + 1);
        }
    }

    private void goToPage(int pageIndex) {
        if (pageIndex < 0 || pageIndex >= pageCount) {
            return;
        }

        PdfService.getInstance().setCurrentPage(filePath, pageIndex);
        currentPage = pageIndex;
        shouldAutoSelectPage = true; // Once user navigates, allow auto-selection
        selectPage(pageIndex);
        updateNavigation();

        // Update projection screen
        projectionScreensUtil.setImage(filePath, ProjectionType.IMAGE, null);
    }

    /**
     * Public method to navigate to a specific PDF page.
     * Accepts 1-based page numbers for user-friendliness.
     *
     * @param page the page number (1-based, from 1 to pageCount)
     */
    public void goToPdfPage(int page) {
        if (pageCount == 0) {
            return;
        }

        // Convert 1-based page number to 0-based index
        // Clamp to valid range
        int pageIndex = Math.max(0, Math.min(page - 1, pageCount - 1));
        goToPage(pageIndex);
    }

    private void selectPage(int pageIndex) {
        clearPreviousSelection();

        if (isValidPageIndex(pageIndex)) {
            PageContainer pageContainer = pageContainers.get(pageIndex);
            highlightPage(pageContainer);
            selectedPageContainer = pageContainer;
            scrollToPage(pageContainer.getMainPane());
        }
    }

    private void clearPreviousSelection() {
        if (selectedPageContainer != null) {
            selectedPageContainer.getHighlightRect().setVisible(false);
        }
    }

    private boolean isValidPageIndex(int pageIndex) {
        return pageIndex >= 0 && pageIndex < pageContainers.size();
    }

    private void highlightPage(PageContainer pageContainer) {
        pageContainer.getHighlightRect().setVisible(true);
    }

    private void scrollToPage(Node node) {
        if (node == null || scrollPane == null) {
            return;
        }

        if (isPageFullyVisible(node)) {
            return;
        }

        ScrollDimensions dimensions = getScrollDimensions(node);
        if (!dimensions.isScrollingNeeded()) {
            return;
        }

        double currentTopVisibleY = getCurrentTopVisibleY(dimensions);
        double scrollValue = calculateScrollValue(dimensions, currentTopVisibleY);
        setScrollValue(scrollValue);
    }

    private static class ScrollDimensions {
        final double nodeY;
        final double nodeHeight;
        final double flowPaneHeight;
        final double scrollPaneHeight;
        final double scrollableHeight;

        ScrollDimensions(double nodeY, double nodeHeight, double flowPaneHeight, double scrollPaneHeight) {
            this.nodeY = nodeY;
            this.nodeHeight = nodeHeight;
            this.flowPaneHeight = flowPaneHeight;
            this.scrollPaneHeight = scrollPaneHeight;
            this.scrollableHeight = flowPaneHeight - scrollPaneHeight;
        }

        boolean isScrollingNeeded() {
            return flowPaneHeight > scrollPaneHeight;
        }
    }

    private ScrollDimensions getScrollDimensions(Node node) {
        double nodeY = node.getBoundsInParent().getMinY();
        double nodeHeight = node.getBoundsInParent().getHeight();
        double flowPaneHeight = flowPane.getHeight();
        double scrollPaneHeight = scrollPane.getViewportBounds().getHeight();
        return new ScrollDimensions(nodeY, nodeHeight, flowPaneHeight, scrollPaneHeight);
    }

    private double getCurrentTopVisibleY(ScrollDimensions dimensions) {
        return scrollPane.getVvalue() * dimensions.scrollableHeight;
    }

    private double calculateScrollValue(ScrollDimensions dimensions, double currentTopVisibleY) {
        if (dimensions.nodeY < currentTopVisibleY) {
            return calculateScrollUpValue(dimensions);
        } else {
            return calculateScrollDownValue(dimensions);
        }
    }

    private double calculateScrollUpValue(ScrollDimensions dimensions) {
        return dimensions.nodeY / dimensions.scrollableHeight;
    }

    private double calculateScrollDownValue(ScrollDimensions dimensions) {
        double targetBottomY = dimensions.nodeY + dimensions.nodeHeight + (0.21 * dimensions.nodeHeight);
        double targetTopY = Math.max(0, targetBottomY - dimensions.scrollPaneHeight);
        return targetTopY / dimensions.scrollableHeight;
    }

    private void setScrollValue(double scrollValue) {
        scrollPane.setVvalue(Math.max(0, Math.min(1, scrollValue)));
    }

    private boolean isPageFullyVisible(Node node) {
        double nodeY = node.getBoundsInParent().getMinY();
        double nodeHeight = node.getBoundsInParent().getHeight();
        double flowPaneHeight = flowPane.getHeight();
        double scrollPaneHeight = scrollPane.getViewportBounds().getHeight();

        if (flowPaneHeight <= scrollPaneHeight) {
            return true; // All content is visible
        }

        double scrollableHeight = flowPaneHeight - scrollPaneHeight;
        double topVisibleY = scrollPane.getVvalue() * scrollableHeight;
        double bottomVisibleY = topVisibleY + scrollPaneHeight;

        return nodeY >= topVisibleY && (nodeY + nodeHeight) <= bottomVisibleY;
    }

    private void updateNavigation() {
        ResourceBundle resourceBundle = Settings.getInstance().getResourceBundle();
        String pageText = resourceBundle.getString("of") + " " + pageCount;
        pageCounterLabel.setText(pageText);
        previousButton.setDisable(currentPage <= 0);
        nextButton.setDisable(currentPage >= pageCount - 1);
        updatePageNumberTextField();
    }

    private void updatePageNumberTextField() {
        if (pageNumberTextField == null) {
            return;
        }

        if (pageCount > 0) {
            // Enable text field if there's at least one page
            pageNumberTextField.setDisable(false);
            // Update text field with 1-based page number if current page is set
            if (currentPage >= 0) {
                pageNumberTextField.setText(String.valueOf(currentPage + 1));
            } else {
                pageNumberTextField.setText("");
            }
        } else {
            pageNumberTextField.setText("");
            pageNumberTextField.setDisable(true);
        }
    }

    public void cleanup() {
        // Mark as closed to prevent new tasks from starting
        isClosed = true;

        if (executorService != null) {
            executorService.shutdown();
            try {
                // Wait for running tasks to complete, with a timeout
                if (!executorService.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    // If tasks don't finish in time, force shutdown
                    executorService.shutdownNow();
                    // Wait a bit more for cancellation to take effect
                    //noinspection ResultOfMethodCallIgnored
                    executorService.awaitTermination(2, java.util.concurrent.TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                // If interrupted, force shutdown
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private static class PageContainer {
        private final Rectangle highlightRect;
        private StackPane mainPane;

        public PageContainer(BorderPane container) {
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
            Rectangle rect = new Rectangle(THUMBNAIL_SIZE, THUMBNAIL_SIZE);
            rect.setFill(Color.rgb(255, 255, 255, 0.21));
            rect.setVisible(false);
            return rect;
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
    }
}

