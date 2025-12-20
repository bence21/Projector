package projector.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.ProjectionType;
import projector.application.Settings;
import projector.controller.util.ImageCacheService;
import projector.controller.util.ImageContainer;
import projector.controller.util.ImageOrderMethod;
import projector.controller.util.PdfService;
import projector.controller.util.ProjectionScreensUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.DoubleConsumer;

import static com.bence.projector.common.util.DebugUtil.gatherTime;
import static com.bence.projector.common.util.DebugUtil.summaryGatheredTime;
import static projector.controller.ProjectionScreenController.drawMiddle;
import static projector.controller.util.ImageCacheService.saveImage;

public class GalleryController {

    private static final String FOLDER_PATH = "gallery";
    private static final String GALLERY_ROOT_NAME = "Gallery";
    private static final Logger LOG = LoggerFactory.getLogger(GalleryController.class);
    private static final Settings settings = Settings.getInstance();

    // Folder icon rendering constants
    private static final double FOLDER_ICON_PADDING = 20.0;
    private static final double FOLDER_ICON_TAB_OFFSET = 30.0;
    private static final double FOLDER_ICON_TAB_CORNER_OFFSET = 10.0;
    private static final double FOLDER_ICON_TEXT_SPACE = 30.0;
    private static final double FOLDER_ICON_TEXT_BOTTOM_OFFSET = 10.0;
    private static final double FOLDER_ICON_TAB_TOP_OFFSET = 10.0;
    private static final double TEXT_WIDTH_MULTIPLIER = 9.5;
    private static final int FOLDER_ICON_FONT_SIZE = 16;
    private static final int FOLDER_ICON_STROKE_WIDTH = 2;
    private static final javafx.scene.paint.Color FOLDER_BODY_COLOR = javafx.scene.paint.Color.rgb(255, 235, 155);
    private static final javafx.scene.paint.Color FOLDER_TAB_COLOR = javafx.scene.paint.Color.rgb(255, 220, 100);
    private static final javafx.scene.paint.Color FOLDER_OUTLINE_COLOR = javafx.scene.paint.Color.rgb(200, 150, 50);
    private static final javafx.scene.paint.Color FOLDER_TEXT_COLOR = javafx.scene.paint.Color.BLACK;
    private final ProjectionScreensUtil projectionScreensUtil = ProjectionScreensUtil.getInstance();
    public BorderPane borderPane;
    private ImageContainer selectedImageContainer; // to keep track of the selected image container
    private FlowPane flowPane = null;
    private List<ImageContainer> containerHolders;
    private boolean pauseSelectByFocus = false;
    private Canvas previewCanvas;
    private WebEngine webEngine;
    private WebView webView;
    private VBox adjustmentPanel = null;
    private BorderPane brightnessBorderPane = null;
    private BorderPane contrastBorderPane = null;
    private BorderPane saturationBorderPane = null;
    private HBox toolHBox = null;
    private HBox breadcrumbBar = null;
    private Image previewImage = null;
    private ScrollPane scrollPane = null;
    private String currentFolderPath = FOLDER_PATH;

    public static void clearCanvas(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setEffect(null);
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public static void drawImageOnCanvas(Image image, Canvas canvas) {
        if (image == null || canvas == null) {
            return;
        }
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawMiddle(image, canvas, gc);
    }

    @SuppressWarnings("CommentedOutCode")
    public static void main(String[] args) {
        // GalleryController galleryController = new GalleryController();
        // for (int i = 1; i <= 464 * 2; ++i) {
        //     // System.out.print("i = " + i + "\t");
        //     int bufferSize = 8192 / 2 * i;
        //     System.out.print(bufferSize + ",");
        //     extracted(galleryController, bufferSize);
        // }
        gatherTime();
        gatherTime();
        summaryGatheredTime();
        // System.out.println(image.getWidth() + "x" + image.getHeight());
        System.out.println("Works");
    }

    private static void loadImagePathToCanvas(String imagePath, Canvas canvas) {
        double scale = getScale();
        Image image = ImageCacheService.getInstance().getImage(imagePath, (int) (canvas.getWidth() * scale), (int) (canvas.getHeight() * scale));
        drawImageOnCanvas(image, canvas);
    }

    private static Image getImageForCanvas(String imagePath, Canvas canvas) {
        return ImageCacheService.getInstance().getImage(imagePath, (int) canvas.getWidth(), (int) canvas.getHeight());
    }

    private static double getScale() {
        double x = Screen.getPrimary().getOutputScaleX();
        return Math.max(x, 0.5);
    }

    private static void bindTextFieldWithSlider(TextField valueTextField, Slider slider) {
        valueTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                double value = Double.parseDouble(newValue);
                if (slider.getValue() != value) {
                    slider.setValue(value);
                }
            } catch (Exception ignored) {
            }
        });
    }

    private void sliderValueBind(Slider slider, TextField valueTextField, DoubleConsumer setter) {
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double x = roundToTwoDecimalPlaces(newValue.doubleValue());
            ProjectionScreenController projectionScreenController = getProjectionScreenController();
            setter.accept(x);
            projectionScreenController.redrawImageForAdjustment();
            valueTextField.setText(x + "");
            reloadPreviewCanvas();
        });
    }

    private static ProjectionScreenController getProjectionScreenController() {
        return MyController.getInstance().getProjectionScreenController();
    }

    private static double roundToTwoDecimalPlaces(double v) {
        double x = 100;
        return Math.round(v * x) / x;
    }

    public static void openExplorer(String folderPath) {
        File file = new File(folderPath);

        if (!file.exists()) {
            System.out.println("Folder does not exist!");
            return;
        }

        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.open(file);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        } else {
            System.out.println("Desktop is not supported on this platform.");
        }
    }

    private static void setLastAccessTime(Path destinationFilePath) throws IOException {
        BasicFileAttributeView attributeView = getBasicFileAttributeView(destinationFilePath);
        attributeView.setTimes(null, getNow(), null);
    }

    private static FileTime getNow() {
        return FileTime.from(Instant.now());
    }

    private static BasicFileAttributeView getBasicFileAttributeView(Path destinationFilePath) {
        return Files.getFileAttributeView(destinationFilePath, BasicFileAttributeView.class);
    }

    private static String getFileNameFromUrlPath(String path) {
        int lastIndexOfSlash = path.replaceAll("/+$", "").lastIndexOf('/');
        if (lastIndexOfSlash >= 0 && lastIndexOfSlash < path.length() - 1) {
            return path.substring(lastIndexOfSlash + 1);
        }
        return path;
    }

    private static boolean isGoodForImage(Dragboard dragboard) {
        if (dragboard.hasImage()) {
            return true;
        }
        if (dragboard.hasUrl()) {
            return !dragboard.getUrl().startsWith("blob");
        }
        return false;
    }

    private static int compareFileTime(FileTime fileTime1, FileTime fileTime2) {
        if (fileTime1 == null) {
            if (fileTime2 != null) {
                return -1;
            }
        } else {
            if (fileTime2 == null) {
                return 1;
            }
            return fileTime1.compareTo(fileTime2);
        }
        return 0;
    }

    private static FileTime getFileLastAccessedTime(File file) {
        BasicFileAttributeView attributeView = getBasicFileAttributeView(file.toPath());
        try {
            BasicFileAttributes basicFileAttributes = attributeView.readAttributes();
            return basicFileAttributes.lastAccessTime();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    private static FileTime getFileLastModifiedTime(File file) {
        BasicFileAttributeView attributeView = getBasicFileAttributeView(file.toPath());
        try {
            BasicFileAttributes basicFileAttributes = attributeView.readAttributes();
            return basicFileAttributes.lastModifiedTime();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    private static FileTime getFileCreatedTime(File file) {
        BasicFileAttributeView attributeView = getBasicFileAttributeView(file.toPath());
        try {
            BasicFileAttributes basicFileAttributes = attributeView.readAttributes();
            return basicFileAttributes.creationTime();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    private void loadImagePathToPreviewCanvas(String imagePath) {
        clearCanvas(previewCanvas);
        previewImage = loadImagePathToCanvasWithColorAdjustments(imagePath, previewCanvas);
    }

    private Image loadImagePathToCanvasWithColorAdjustments(String imagePath, Canvas canvas) {
        if (isMediaFile(imagePath)) {
            return null;
        }
        Image image = loadImageForCanvas(imagePath, canvas);
        if (image != null) {
            getProjectionScreenController().drawImageOnCanvasColorAdjustments(image, canvas);
        }
        return image;
    }

    private Image loadImageForCanvas(String imagePath, Canvas canvas) {
        if (PdfService.isPdfFile(imagePath)) {
            // Render current PDF page for preview
            float dpi = (float) (Math.max(canvas.getWidth(), canvas.getHeight()) * 72.0 / 400.0);
            return PdfService.getInstance().renderCurrentPageAsImage(imagePath, dpi);
        } else {
            return getImageForCanvas(imagePath, canvas);
        }
    }

    private void reloadPreviewCanvas() {
        if (previewImage == null || previewCanvas == null) {
            return;
        }
        clearCanvas(previewCanvas);
        getProjectionScreenController().drawImageOnCanvasColorAdjustments(previewImage, previewCanvas);
    }

    public void onTabOpened() {
        HBox breadcrumb = getBreadcrumbBar();
        HBox hBox = getToolHBox();
        BorderPane borderPane1 = new BorderPane();
        VBox topBox = new VBox();
        if (breadcrumb != null) {
            topBox.getChildren().add(breadcrumb);
        }
        topBox.getChildren().add(hBox);
        borderPane1.setTop(topBox);
        borderPane1.setCenter(createGalleryPane()); // TODO: this should be optimized
        borderPane.setCenter(borderPane1);
        borderPane.setRight(createRightBorderPane());
        borderPane.setOnDragOver(event -> {
            Dragboard dragboard = event.getDragboard();
            event.acceptTransferModes(TransferMode.ANY);
            if (dragboard.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            } else if (isGoodForImage(dragboard)) {
                event.acceptTransferModes(TransferMode.COPY);
            } else {
                event.consume();
            }
        });

        // Handle drop
        borderPane.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;

            if (dragboard.hasFiles()) {
                List<File> files = dragboard.getFiles();
                for (File file : files) {
                    try {
                        Path destinationFolder = Path.of(currentFolderPath);
                        // Create the destination file path within the folder
                        Path destinationFilePath = destinationFolder.resolve(file.getName());
                        Files.copy(file.toPath(), destinationFilePath, StandardCopyOption.REPLACE_EXISTING);

                        setLastAccessTime(destinationFilePath);
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
                Platform.runLater(this::onTabOpened);
            } else if (isGoodForImage(dragboard)) {
                Image image = getImageFromDragBoard(dragboard);
                onImageDropped(image);
                if (image != null) {
                    success = true;
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private BorderPane createRightBorderPane() {
        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(0, 10, 10, 0));
        Canvas previewCanvas = createPreviewCanvas();
        Pane borderPaneForCanvas = new Pane(previewCanvas);
        borderPane.setCenter(borderPaneForCanvas);
        borderPane.setBottom(getAdjustmentPanel());
        borderPaneForCanvas.setSnapToPixel(false);
        borderPane.setSnapToPixel(false); // so we don't go to an infinite loop because of previewCanvas.setWidth(newValue.doubleValue());
        borderPaneForCanvas.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (!borderPane.isSnapToPixel()) {
                previewCanvas.setWidth(newValue.doubleValue()); // snapToPixel should be false
            }
        });
        borderPaneForCanvas.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (!borderPane.isSnapToPixel()) {
                previewCanvas.setHeight(newValue.doubleValue()); // snapToPixel should be false
            }
        });
        return borderPane;
    }

    private VBox getAdjustmentPanel() {
        if (adjustmentPanel == null) {
            adjustmentPanel = new VBox();
            adjustmentPanel.setSpacing(10.0);
            adjustmentPanel.setPadding(new Insets(10, 0, 0, 0));
            ObservableList<Node> children = adjustmentPanel.getChildren();
            children.add(getBrightnessBorderPane());
            children.add(getContrastBorderPane());
            children.add(getSaturationBorderPane());
        }
        return this.adjustmentPanel;
    }

    private Result getColorAdjustmentPane(String text) {
        BorderPane brightnessBorderPane = new BorderPane();
        Label brightness = new Label(text);
        BorderPane.setAlignment(brightness, javafx.geometry.Pos.CENTER);
        brightnessBorderPane.setLeft(brightness);
        TextField brightnessValueTextField = new TextField("0.0");
        brightnessValueTextField.setPrefWidth(50.0);
        BorderPane.setAlignment(brightnessValueTextField, javafx.geometry.Pos.CENTER);
        brightnessBorderPane.setRight(brightnessValueTextField);
        Slider brightnessSlider = colorAdjustmentSlider();
        brightnessBorderPane.setBottom(brightnessSlider);
        return new Result(brightnessBorderPane, brightnessValueTextField, brightnessSlider);
    }

    private Result getColorAdjustmentPane2(String text, DoubleConsumer setter) {
        Result result = getColorAdjustmentPane(text);
        Slider slider = result.slider();
        TextField valueTextField = result.valueTextField();
        sliderValueBind(slider, valueTextField, setter);
        bindTextFieldWithSlider(valueTextField, slider);
        return result;
    }

    private BorderPane getBrightnessBorderPane() {
        if (brightnessBorderPane == null) {
            Result result = getColorAdjustmentPane2("Brightness", getProjectionScreenController()::setBrightness);
            brightnessBorderPane = result.borderPane();
        }
        return brightnessBorderPane;
    }

    private BorderPane getContrastBorderPane() {
        if (contrastBorderPane == null) {
            Result result = getColorAdjustmentPane2("Contrast", getProjectionScreenController()::setContrast);
            contrastBorderPane = result.borderPane();
        }
        return contrastBorderPane;
    }

    private BorderPane getSaturationBorderPane() {
        if (saturationBorderPane == null) {
            Result result = getColorAdjustmentPane2("Saturation", getProjectionScreenController()::setSaturation);
            saturationBorderPane = result.borderPane();
        }
        return saturationBorderPane;
    }

    private Slider colorAdjustmentSlider() {
        Slider slider = new Slider();
        slider.setMax(1.0);
        slider.setMin(-1.0);
        slider.setValue(0.0);
        slider.setBlockIncrement(0.1);
        return slider;
    }

    private HBox getToolHBox() {
        toolHBox = createToolHBox();

        Button addImagesButton = createAddImagesButton();
        Button openFileButton = createOpenFileButton();
        ComboBox<ImageOrderMethod> sortComboBox = getSortComboBox();

        toolHBox.getChildren().addAll(addImagesButton, openFileButton, sortComboBox);

        return toolHBox;
    }

    private HBox createToolHBox() {
        HBox hBox = new HBox();
        hBox.setSpacing(10);
        hBox.setPadding(new Insets(10, 10, 0, 10));
        return hBox;
    }

    private Button createAddImagesButton() {
        Button addImagesButton = new Button("Add images");
        addImagesButton.setOnAction((event) -> {
            try {
                Files.createDirectories(Paths.get(currentFolderPath));
            } catch (IOException ignored) {
            }
            openExplorer(currentFolderPath);
        });
        return addImagesButton;
    }

    private Button createOpenFileButton() {
        Button openFileButton = new Button("Open file");
        openFileButton.setOnAction((event -> {
            if (selectedImageContainer == null) {
                return;
            }
            openExplorer(selectedImageContainer.getFileImagePath());
        }));
        return openFileButton;
    }

    private boolean shouldShowBackButton() {
        try {
            File currentFolder = new File(currentFolderPath);
            File rootFolder = new File(FOLDER_PATH);
            String currentAbsolutePath = currentFolder.getAbsolutePath();
            String rootAbsolutePath = rootFolder.getAbsolutePath();
            return !currentAbsolutePath.equals(rootAbsolutePath);
        } catch (Exception e) {
            LOG.error("Error comparing folder paths", e);
            return !currentFolderPath.equals(FOLDER_PATH);
        }
    }

    private HBox getBreadcrumbBar() {
        if (breadcrumbBar == null) {
            breadcrumbBar = createBreadcrumbBar();
        }
        return breadcrumbBar;
    }

    private HBox createBreadcrumbBar() {
        List<BreadcrumbItem> pathItems = buildBreadcrumbPath();

        // Hide breadcrumb if only at Gallery root
        if (pathItems.size() <= 1) {
            return null;
        }

        HBox breadcrumb = initializeBreadcrumbContainer();
        boolean isDarkTheme = settings.isDarkTheme();

        addUpButtonIfNeeded(breadcrumb, isDarkTheme);
        addBreadcrumbPathItems(breadcrumb, pathItems, isDarkTheme);

        return breadcrumb;
    }

    private HBox initializeBreadcrumbContainer() {
        HBox breadcrumb = new HBox();
        breadcrumb.setSpacing(5);
        breadcrumb.setPadding(new Insets(5, 10, 5, 10));
        breadcrumb.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return breadcrumb;
    }

    private void addUpButtonIfNeeded(HBox breadcrumb, boolean isDarkTheme) {
        if (shouldShowBackButton()) {
            Button upButton = createUpButton();
            breadcrumb.getChildren().add(upButton);

            Label separator = createSeparator("|", isDarkTheme);
            breadcrumb.getChildren().add(separator);
        }
    }

    private void addBreadcrumbPathItems(HBox breadcrumb, List<BreadcrumbItem> pathItems, boolean isDarkTheme) {
        for (int i = 0; i < pathItems.size(); i++) {
            BreadcrumbItem item = pathItems.get(i);

            if (i > 0) {
                Label separator = createSeparator(">", isDarkTheme);
                breadcrumb.getChildren().add(separator);
            }

            Hyperlink link = createBreadcrumbLink(item, i == pathItems.size() - 1, isDarkTheme);
            breadcrumb.getChildren().add(link);
        }
    }

    private Label createSeparator(String text, boolean isDarkTheme) {
        Label separator = new Label(text);
        String color = isDarkTheme ? "#aaaaaa" : "#666666";
        separator.setStyle("-fx-text-fill: " + color + ";");
        return separator;
    }

    private Hyperlink createBreadcrumbLink(BreadcrumbItem item, boolean isCurrentFolder, boolean isDarkTheme) {
        Hyperlink link = new Hyperlink(item.name());
        link.setOnAction(event -> navigateToFolder(item.path()));

        if (isCurrentFolder) {
            styleCurrentFolderLink(link, isDarkTheme);
        } else {
            styleParentFolderLink(link, isDarkTheme);
        }

        return link;
    }

    private void styleCurrentFolderLink(Hyperlink link, boolean isDarkTheme) {
        String color = isDarkTheme ? "#ffffff" : "#000000";
        link.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }

    private void styleParentFolderLink(Hyperlink link, boolean isDarkTheme) {
        String normalColor = isDarkTheme ? "#7cc8ff" : "#0066cc";
        String hoverColor = isDarkTheme ? "#94d2ff" : "#0052a3";

        link.setStyle("-fx-text-fill: " + normalColor + ";");
        link.setOnMouseEntered(e -> link.setStyle("-fx-text-fill: " + hoverColor + "; -fx-underline: true;"));
        link.setOnMouseExited(e -> link.setStyle("-fx-text-fill: " + normalColor + "; -fx-underline: false;"));
    }

    private Button createUpButton() {
        Button upButton = new Button("↑ Up");
        upButton.setOnAction((event) -> {
            File currentFolder = new File(currentFolderPath);
            File parentFolder = currentFolder.getParentFile();
            if (parentFolder != null) {
                navigateToFolder(parentFolder.getAbsolutePath());
            }
        });
        return upButton;
    }

    private List<BreadcrumbItem> buildBreadcrumbPath() {
        List<BreadcrumbItem> items = new ArrayList<>();

        try {
            String rootAbsolutePath = getRootAbsolutePath();
            String currentAbsolutePath = getCurrentAbsolutePath();

            items.add(new BreadcrumbItem(GALLERY_ROOT_NAME, rootAbsolutePath));

            if (!currentAbsolutePath.equals(rootAbsolutePath)) {
                addSubfolderPathComponents(items, rootAbsolutePath, currentAbsolutePath);
            }
        } catch (Exception e) {
            LOG.error("Error building breadcrumb path", e);
            addFallbackBreadcrumbItem(items);
        }

        return items;
    }

    private String getRootAbsolutePath() {
        File rootFolder = new File(FOLDER_PATH);
        return rootFolder.getAbsolutePath();
    }

    private String getCurrentAbsolutePath() {
        File currentFolder = new File(currentFolderPath);
        return currentFolder.getAbsolutePath();
    }

    private void addSubfolderPathComponents(List<BreadcrumbItem> items, String rootAbsolutePath, String currentAbsolutePath) {
        Path rootPath = Paths.get(rootAbsolutePath);
        Path currentPath = Paths.get(currentAbsolutePath);

        try {
            addPathComponentsUsingRelativize(items, rootPath, currentPath);
        } catch (IllegalArgumentException e) {
            // Paths are not relative (different roots), build from current path
            addPathComponentsUsingParentTraversal(items, rootPath, currentPath, rootAbsolutePath);
        }
    }

    private void addPathComponentsUsingRelativize(List<BreadcrumbItem> items, Path rootPath, Path currentPath) {
        Path relativePath = rootPath.relativize(currentPath);
        if (relativePath.toString().isEmpty()) {
            return; // Already at root
        }

        Path current = rootPath;
        for (Path component : relativePath) {
            current = current.resolve(component);
            String folderName = component.toString();
            items.add(new BreadcrumbItem(folderName, current.toString()));
        }
    }

    private void addPathComponentsUsingParentTraversal(List<BreadcrumbItem> items, Path rootPath, Path currentPath, String rootAbsolutePath) {
        List<String> pathComponents = collectPathComponents(currentPath, rootAbsolutePath);

        Path current = rootPath;
        for (String component : pathComponents) {
            current = current.resolve(component);
            items.add(new BreadcrumbItem(component, current.toString()));
        }
    }

    private List<String> collectPathComponents(Path currentPath, String rootAbsolutePath) {
        List<String> pathComponents = new ArrayList<>();
        Path path = currentPath;

        while (path != null && !path.toString().equals(rootAbsolutePath)) {
            pathComponents.add(0, path.getFileName().toString());
            path = path.getParent();
            if (path == null) {
                break;
            }
        }

        return pathComponents;
    }

    private void addFallbackBreadcrumbItem(List<BreadcrumbItem> items) {
        String folderName = new File(currentFolderPath).getName();
        if (folderName.isEmpty()) {
            folderName = GALLERY_ROOT_NAME;
        }
        items.add(new BreadcrumbItem(folderName, currentFolderPath));
    }

    private record BreadcrumbItem(String name, String path) {
    }

    private ComboBox<ImageOrderMethod> getSortComboBox() {
        ComboBox<ImageOrderMethod> sortComboBox = new ComboBox<>();
        ObservableList<ImageOrderMethod> items = sortComboBox.getItems();
        items.add(ImageOrderMethod.BY_LAST_ACCESSED);
        items.add(ImageOrderMethod.ASCENDING_BY_TITLE);
        items.add(ImageOrderMethod.DESCENDING_BY_TITLE);
        items.add(ImageOrderMethod.BY_MODIFIED_DATE);
        items.add(ImageOrderMethod.BY_CREATED_DATE);
        SingleSelectionModel<ImageOrderMethod> selectionModel = sortComboBox.getSelectionModel();
        selectionModel.select(settings.getImageOrderMethod());
        selectionModel.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            settings.setImageOrderMethod(newValue);
            settings.save();
            onTabOpened();
        });
        return sortComboBox;
    }

    private void onImageDropped(Image image) {
        if (image == null) {
            return;
        }
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        saveImage(bufferedImage, currentFolderPath + "/" + getFileNameFromUrlPath(image.getUrl()));
        onTabOpened();
    }

    private Image getImageFromDragBoard(Dragboard dragboard) {
        Image image = null;
        try {
            image = new Image(dragboard.getUrl());
        } catch (Exception e) {
            WebEngine webEngine = getWebEngine();
            webEngine.setJavaScriptEnabled(true);
            String content = "<html><body><img id='image' src='" + dragboard.getUrl() + "' crossorigin='anonymous'></body><script>" + "function getImageData() {" + "  var img = document.getElementById('image');" + "  var canvas = document.createElement('canvas');" + "  canvas.width = img.width; canvas.height = img.height;" + "  var ctx = canvas.getContext('2d');" + "  ctx.drawImage(img, 0, 0);" + "  var imageData = canvas.toDataURL();" + "  return imageData;" + "}" + "</script></html>";
            // Execute JavaScript to fetch the image data and communicate with JavaFX
            webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == javafx.concurrent.Worker.State.SUCCEEDED) {
                    // Execute JavaScript function to get image data
                    String result = (String) webEngine.executeScript("getImageData()");
                    Image fxImage = convertToJavaFXImage(result);
                    onImageDropped(fxImage);
                }
            });
            webEngine.loadContent(content);
        }
        return image;
    }

    private WebEngine getWebEngine() {
        if (webView == null) {
            webView = new WebView();
        }
        if (webEngine == null) {
            webEngine = webView.getEngine();
        }
        return webEngine;
    }

    private Image convertToJavaFXImage(String base64) {
        try {
            if (base64 != null && base64.startsWith("data:image")) {
                // Remove data URI prefix
                String base64Data = base64.substring(base64.indexOf(',') + 1).replaceAll("\r\n", "");
                byte[] imageBytes = Base64.getDecoder().decode(base64Data);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
                return new Image(inputStream);
            } else {
                LOG.warn("Invalid or null image data");
            }
        } catch (Exception e) {
            LOG.error("Error decoding image data: {}", e.getMessage(), e);
        }
        return null;
    }

    private Canvas createPreviewCanvas() {
        if (previewCanvas == null) {
            previewCanvas = new Canvas(400, 400);
            ChangeListener<Number> listener = (observable, oldValue, newValue) -> reloadPreviewCanvas();
            previewCanvas.widthProperty().addListener(listener);
            previewCanvas.heightProperty().addListener(listener);
        }
        return previewCanvas;
    }

    private StackPane createGalleryPane() {
        StackPane galleryPane = new StackPane();
        galleryPane.setPadding(new Insets(10));
        galleryPane.setFocusTraversable(true);

        initializeFlowPane();
        populateGalleryItems();

        ScrollPane scrollPane = createScrollPane();
        galleryPane.getChildren().addAll(scrollPane);
        return galleryPane;
    }

    private void initializeFlowPane() {
        flowPane = new FlowPane(10, 10);
        flowPane.setPadding(new Insets(0));
        flowPane.setPrefWrapLength(780);
        flowPane.setOnKeyPressed(this::handleKeyPress);
        flowPane.setFocusTraversable(true);
        flowPane.setHgap(10);
        containerHolders = new ArrayList<>();
    }

    private void populateGalleryItems() {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        List<String> paths = getImagePathsFromFolder(currentFolderPath);
        ObservableList<Node> flowPaneChildren = flowPane.getChildren();

        for (String path : paths) {
            StackPane itemPane = createGalleryItem(path, executorService);
            flowPaneChildren.add(itemPane);
        }

        executorService.shutdown();
    }

    private StackPane createGalleryItem(String path, ExecutorService executorService) {
        File file = new File(path);
        boolean isFolder = file.isDirectory();
        int itemCount = isFolder ? countItemsInFolder(path) : 0;

        Canvas canvas = createItemCanvas(path, isFolder, itemCount, executorService);
        ImageContainer imageContainer = createImageContainer(path, canvas, isFolder);
        return createItemStackPane(path, imageContainer, isFolder);
    }

    private Canvas createItemCanvas(String path, boolean isFolder, int itemCount, ExecutorService executorService) {
        Canvas canvas = new Canvas(200, 200);

        if (isFolder) {
            renderFolderIcon(canvas, itemCount);
        } else {
            executorService.submit(() -> {
                try {
                    loadImagePathToCanvas(path, canvas);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
        }

        return canvas;
    }

    private ImageContainer createImageContainer(String path, Canvas canvas, boolean isFolder) {
        Label filenameLabel = new Label(getFileNameFromPath(path));
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(canvas);
        borderPane.setBottom(filenameLabel);
        BorderPane.setAlignment(filenameLabel, javafx.geometry.Pos.CENTER);
        BorderPane.setMargin(canvas, new Insets(5));

        ImageContainer imageContainer = new ImageContainer(borderPane);
        imageContainer.setFileImagePath(path);
        imageContainer.setFolder(isFolder);

        return imageContainer;
    }

    private StackPane createItemStackPane(String path, ImageContainer imageContainer, boolean isFolder) {
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(imageContainer.getHighlightRect(), imageContainer.getContainer());

        setupItemClickHandler(stackPane, path, imageContainer, isFolder);
        setupItemFocusHandler(stackPane, imageContainer, isFolder);

        stackPane.setFocusTraversable(true);
        containerHolders.add(imageContainer);
        imageContainer.setMainPane(stackPane);

        return stackPane;
    }

    private void setupItemClickHandler(StackPane stackPane, String path, ImageContainer imageContainer, boolean isFolder) {
        stackPane.setOnMouseClicked(event -> {
            try {
                pauseSelectByFocus = true;
                stackPane.requestFocus();
                if (isFolder) {
                    navigateToFolder(path);
                } else {
                    selectImageContainer(imageContainer);
                }
            } finally {
                pauseSelectByFocus = false;
            }
        });
    }

    private void setupItemFocusHandler(StackPane stackPane, ImageContainer imageContainer, boolean isFolder) {
        stackPane.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (pauseSelectByFocus) {
                return;
            }
            if (newValue != null && newValue && !isFolder && selectedImageContainer != imageContainer) {
                selectImageContainer(imageContainer);
            }
        });
    }

    private ScrollPane createScrollPane() {
        ScrollPane scrollPane = new ScrollPane(flowPane);
        this.scrollPane = scrollPane;
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFocusTraversable(false);
        scrollPane.setOnKeyPressed(this::handleKeyPress);
        return scrollPane;
    }

    private void navigateToFolder(String folderPath) {
        currentFolderPath = folderPath;
        selectedImageContainer = null;
        toolHBox = null; // Force recreation to update back button
        breadcrumbBar = null; // Force recreation to update breadcrumb
        onTabOpened();
    }

    @SuppressWarnings("unused")
    private Image convertToJavaFXImage(BufferedImage image) {
        WritableImage writableImage = new WritableImage(image.getWidth(), image.getHeight());
        gatherTime();
        javafx.scene.image.PixelWriter pixelWriter = writableImage.getPixelWriter();
        gatherTime();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                pixelWriter.setArgb(x, y, image.getRGB(x, y));
            }
        }
        gatherTime();
        return writableImage;
    }

    private boolean selectImageContainer(ImageContainer imageContainer) {
        try {
            // Skip folders - only images can be selected
            if (imageContainer.isFolder()) {
                return false;
            }
            if (selectedImageContainer != null) {
                selectedImageContainer.getHighlightRect().setVisible(false);
            }
            if (selectedImageContainer != imageContainer) {
                imageContainer.getHighlightRect().setVisible(true);
                selectedImageContainer = imageContainer;
                String nextFileImagePath = getNextFileImagePath();
                String fileImagePath = imageContainer.getFileImagePath();
                setLastAccessTime(Path.of(fileImagePath));

                if (openMultiPagePdfIfNeeded(fileImagePath)) {
                    return true;
                }

                projectionScreensUtil.setImage(fileImagePath, ProjectionType.IMAGE, nextFileImagePath);
                loadImagePathToPreviewCanvas(fileImagePath);
                return true;
            } else {
                selectedImageContainer = null;
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return false;
    }

    private boolean openMultiPagePdfIfNeeded(String filePath) {
        if (PdfService.isPdfFile(filePath)) {
            int pageCount = PdfService.getInstance().getPageCount(filePath);
            if (pageCount > 1) {
                MyController.getInstance().openPdfViewerTab(filePath);
                return true;
            }
        }
        return false;
    }

    private String getNextFileImagePath() {
        int selectedIndex = getSelectedImageIndex();
        // Find next image (skip folders)
        for (int i = selectedIndex + 1; i < containerHolders.size(); i++) {
            ImageContainer container = getImageContainer(i);
            if (container != null && !container.isFolder()) {
                return container.getFileImagePath();
            }
        }
        return null;
    }

    private List<String> getImagePathsFromFolder(@SuppressWarnings("SameParameterValue") String folderPath) {
        List<String> paths = new ArrayList<>();
        File folder = new File(folderPath);
        List<File> orderedFiles = new ArrayList<>();
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory() || isImageFile(file.getName())) {
                        orderedFiles.add(file);
                    }
                }
            }
        }
        orderedFiles.sort(getComparator());
        for (File file : orderedFiles) {
            paths.add(file.getAbsolutePath());
        }
        return paths;
    }

    private int countItemsInFolder(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            return 0;
        }
        File[] files = folder.listFiles();
        if (files == null) {
            return 0;
        }
        return (int) Arrays.stream(files)
                .filter(file -> isImageFile(file.getName()))
                .count();
    }

    private void renderFolderIcon(Canvas canvas, int itemCount) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw folder icon (simple representation)
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double padding = FOLDER_ICON_PADDING;
        double folderWidth = width - 2 * padding;
        double folderHeight = height - 2 * padding - FOLDER_ICON_TEXT_SPACE;

        double bodyTop = padding + FOLDER_ICON_TAB_TOP_OFFSET;

        // Folder body (rectangle)
        gc.setFill(FOLDER_BODY_COLOR);
        gc.fillRect(padding, bodyTop, folderWidth, folderHeight);

        // Folder tab (trapezoid-like shape)
        gc.setFill(FOLDER_TAB_COLOR);
        double[] xPoints = {
                padding,
                padding + FOLDER_ICON_TAB_OFFSET,
                padding + folderWidth - FOLDER_ICON_TAB_CORNER_OFFSET,
                padding + folderWidth
        };
        double[] yPoints = {
                bodyTop,
                padding,
                padding,
                bodyTop
        };
        gc.fillPolygon(xPoints, yPoints, 4);

        // Folder outline
        gc.setStroke(FOLDER_OUTLINE_COLOR);
        gc.setLineWidth(FOLDER_ICON_STROKE_WIDTH);
        gc.strokeRect(padding, bodyTop, folderWidth, folderHeight);
        gc.strokePolyline(xPoints, yPoints, 4);

        // Item count text
        gc.setFill(FOLDER_TEXT_COLOR);
        Font font = Font.font(FOLDER_ICON_FONT_SIZE);
        gc.setFont(font);
        String countText = formatItemCount(itemCount);
        // Approximate text width (roughly 9-10 pixels per character for 16pt font)
        // This is simpler and more efficient than creating a scene for measurement
        double textWidth = countText.length() * TEXT_WIDTH_MULTIPLIER;
        gc.fillText(countText, (width - textWidth) / 2, height - FOLDER_ICON_TEXT_BOTTOM_OFFSET);
    }

    private String formatItemCount(int count) {
        return count + " item" + (count != 1 ? "s" : "");
    }

    private static Comparator<File> getComparator() {
        ImageOrderMethod imageOrderMethod = settings.getImageOrderMethod();
        Comparator<File> secondaryComparator = switch (imageOrderMethod) {
            case ASCENDING_BY_TITLE -> (o1, o2) -> naturalCompare(o1.getName(), o2.getName());
            case DESCENDING_BY_TITLE -> (o1, o2) -> naturalCompare(o2.getName(), o1.getName());
            case BY_MODIFIED_DATE -> (o1, o2) -> {
                FileTime fileLastModifiedTime1 = getFileLastModifiedTime(o1);
                FileTime fileLastModifiedTime2 = getFileLastModifiedTime(o2);
                return compareFileTime(fileLastModifiedTime2, fileLastModifiedTime1);
            };
            case BY_CREATED_DATE -> (o1, o2) -> {
                FileTime fileCreatedTime1 = getFileCreatedTime(o1);
                FileTime fileCreatedTime2 = getFileCreatedTime(o2);
                return compareFileTime(fileCreatedTime2, fileCreatedTime1);
            };
            default -> (o1, o2) -> {
                FileTime fileLastAccessedTime1 = getFileLastAccessedTime(o1);
                FileTime fileLastAccessedTime2 = getFileLastAccessedTime(o2);
                int lastAccessedTimeCompare = compareFileTime(fileLastAccessedTime2, fileLastAccessedTime1);
                if (lastAccessedTimeCompare == 0) {
                    FileTime fileLastModifiedTime1 = getFileLastModifiedTime(o1);
                    FileTime fileLastModifiedTime2 = getFileLastModifiedTime(o2);
                    return compareFileTime(fileLastModifiedTime2, fileLastModifiedTime1);
                }
                return lastAccessedTimeCompare;
            };
        };

        // First compare by type (folders first, then files), then by secondary comparator
        return Comparator.comparing((File file) -> file.isDirectory() ? 0 : 1)
                .thenComparing(secondaryComparator);
    }

    private static int naturalCompare(String s1, String s2) {
        if (s1 == null && s2 == null) {
            return 0;
        }
        if (s1 == null) {
            return -1;
        }
        if (s2 == null) {
            return 1;
        }

        // Strip accents for comparison while preserving structure
        String normalized1 = projector.utils.StringUtils.stripAccentsPreservingStructure(s1);
        String normalized2 = projector.utils.StringUtils.stripAccentsPreservingStructure(s2);

        int len1 = normalized1.length();
        int len2 = normalized2.length();
        int i1 = 0;
        int i2 = 0;

        while (i1 < len1 && i2 < len2) {
            char c1 = normalized1.charAt(i1);
            char c2 = normalized2.charAt(i2);

            boolean isDigit1 = Character.isDigit(c1);
            boolean isDigit2 = Character.isDigit(c2);

            if (isDigit1 && isDigit2) {
                // Both are digits - parse and compare as numbers
                int num1 = 0;
                int num2 = 0;

                // Parse number from normalized1
                while (i1 < len1 && Character.isDigit(normalized1.charAt(i1))) {
                    num1 = num1 * 10 + Character.getNumericValue(normalized1.charAt(i1));
                    i1++;
                }

                // Parse number from normalized2
                while (i2 < len2 && Character.isDigit(normalized2.charAt(i2))) {
                    num2 = num2 * 10 + Character.getNumericValue(normalized2.charAt(i2));
                    i2++;
                }

                if (num1 != num2) {
                    return Integer.compare(num1, num2);
                }
            } else if (isDigit1) {
                // normalized1 has digit, normalized2 has non-digit - digits come after non-digits
                return 1;
            } else if (isDigit2) {
                // normalized2 has digit, normalized1 has non-digit - digits come after non-digits
                return -1;
            } else {
                // Both are non-digits - compare as characters (case-insensitive)
                int compare = Character.compare(Character.toLowerCase(c1), Character.toLowerCase(c2));
                if (compare != 0) {
                    return compare;
                }
                i1++;
                i2++;
            }
        }

        // One string is shorter
        return Integer.compare(len1, len2);
    }

    private boolean isImageFile(String fileName) {
        String[] imageExtensions = {".jpg", ".jpeg", ".png", ".gif", ".mp4", ".pdf"};
        for (String extension : imageExtensions) {
            if (fileName.toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMediaFile(String path) {
        if (path == null) {
            return false;
        }
        return path.toLowerCase().endsWith(".mp4");
    }

    private String getFileNameFromPath(String path) {
        File file = new File(path);
        return file.getName();
    }

    public void handleKeyPress(KeyEvent event) {
        if (selectedImageContainer != null) {
            KeyCode keyCode = event.getCode();
            if (keyCode == KeyCode.LEFT || keyCode == KeyCode.PAGE_UP) {
                setPrevious();
                event.consume();
            } else if (keyCode == KeyCode.RIGHT || keyCode == KeyCode.PAGE_DOWN) {
                setNext();
                event.consume();
            }
        }
    }

    private int getSelectedImageIndex() {
        return getSelectedImageIndex(selectedImageContainer);
    }

    private int getSelectedImageIndex(ImageContainer imageContainer) {
        if (imageContainer != null) {
            StackPane mainPane = imageContainer.getMainPane();
            return flowPane.getChildren().indexOf(mainPane);
        }
        return -1;
    }

    private void setSelectedImage(int index) {
        try {
            pauseSelectByFocus = true;
            StackPane stackPane = (StackPane) this.flowPane.getChildren().get(index);
            stackPane.requestFocus();
            if (containerHolders == null || containerHolders.size() <= index) {
                return;
            }
            ImageContainer imageContainer = getImageContainer(index);
            if (imageContainer != null && !imageContainer.isFolder()) {
                if (selectImageContainer(imageContainer)) {
                    Bounds boundsInParent = stackPane.getBoundsInParent();
                    double yPos = boundsInParent.getMinY();
                    double scrollHeight = flowPane.getHeight() - scrollPane.getHeight();
                    double scrollPositionTop = yPos / scrollHeight;
                    double scrollPositionBottom = scrollPositionTop + (-scrollPane.getHeight() + boundsInParent.getHeight()) / scrollHeight;
                    double scrollPaneVvalue = scrollPane.getVvalue();
                    if (scrollPaneVvalue > scrollPositionTop) {
                        scrollPane.setVvalue(scrollPositionTop);
                    } else if (scrollPaneVvalue < scrollPositionBottom) {
                        scrollPane.setVvalue(scrollPositionBottom);
                    }
                }
            }
        } finally {
            pauseSelectByFocus = false;
        }
    }

    private ImageContainer getImageContainer(int index) {
        if (index < 0 || index >= containerHolders.size()) {
            return null;
        }
        return containerHolders.get(index);
    }

    public void setNext() {
        int selectedIndex = getSelectedImageIndex();
        // Find next image (skip folders)
        for (int i = selectedIndex + 1; i < containerHolders.size(); i++) {
            ImageContainer container = getImageContainer(i);
            if (container != null && !container.isFolder()) {
                setSelectedImage(i);
                return;
            }
        }
    }

    public void setPrevious() {
        int selectedIndex = getSelectedImageIndex();
        // Find previous image (skip folders)
        for (int i = selectedIndex - 1; i >= 0; i--) {
            ImageContainer container = getImageContainer(i);
            if (container != null && !container.isFolder()) {
                setSelectedImage(i);
                return;
            }
        }
    }

    private record Result(BorderPane borderPane, TextField valueTextField, Slider slider) {

    }

}
