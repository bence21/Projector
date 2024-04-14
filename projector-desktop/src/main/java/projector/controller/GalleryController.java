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
    private static final Logger LOG = LoggerFactory.getLogger(GalleryController.class);
    private static final Settings settings = Settings.getInstance();
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
    private Image previewImage = null;
    private ScrollPane scrollPane = null;

    public static void clearCanvas(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setEffect(null);
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public static void drawImageOnCanvas(Image image, Canvas canvas) {
        if (image == null) {
            return;
        }
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawMiddle(image, canvas, gc);
    }

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
        Image image = getImageForCanvas(imagePath, canvas);
        getProjectionScreenController().drawImageOnCanvasColorAdjustments(image, canvas);
        return image;
    }

    private void reloadPreviewCanvas() {
        if (previewImage == null || previewCanvas == null) {
            return;
        }
        clearCanvas(previewCanvas);
        getProjectionScreenController().drawImageOnCanvasColorAdjustments(previewImage, previewCanvas);
    }

    public void onTabOpened() {
        // if (flowPane != null) {
        //     return;
        // }
        HBox hBox = getToolHBox();
        BorderPane borderPane1 = new BorderPane();
        borderPane1.setTop(hBox);
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
                        Path destinationFolder = Path.of(FOLDER_PATH);
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
        if (toolHBox == null) {
            toolHBox = new HBox();
            toolHBox.setSpacing(10);
            toolHBox.setPadding(new Insets(10, 10, 0, 10));
            Button addImagesButton = new Button("Add images");
            Button openFileButton = new Button("Open file");
            ComboBox<ImageOrderMethod> sortComboBox = getSortComboBox();
            addImagesButton.setOnAction((event) -> {
                try {
                    Files.createDirectories(Paths.get(FOLDER_PATH));
                } catch (IOException ignored) {
                }
                openExplorer(FOLDER_PATH);
            });
            openFileButton.setOnAction((event -> {
                if (selectedImageContainer == null) {
                    return;
                }
                openExplorer(selectedImageContainer.getFileImagePath());
            }));
            toolHBox.getChildren().addAll(addImagesButton, openFileButton, sortComboBox);
        }
        return toolHBox;
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
        saveImage(bufferedImage, FOLDER_PATH + "/" + getFileNameFromUrlPath(image.getUrl()));
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
            LOG.error("Error decoding image data: " + e.getMessage(), e);
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

        flowPane = new FlowPane(10, 10);
        flowPane.setPadding(new Insets(0));
        flowPane.setPrefWrapLength(780);
        flowPane.setOnKeyPressed(this::handleKeyPress);
        flowPane.setFocusTraversable(true);
        flowPane.setHgap(10);
        containerHolders = new ArrayList<>();
        ObservableList<Node> flowPaneChildren = flowPane.getChildren();

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        List<String> imagePaths = getImagePathsFromFolder(FOLDER_PATH);
        for (String imagePath : imagePaths) {

            Canvas canvas = new Canvas(200, 200);
            // Image image = new Image(fileImagePath);
            executorService.submit(() -> {
                try {
                    loadImagePathToCanvas(imagePath, canvas);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });

            Label filenameLabel = new Label(getFileNameFromPath(imagePath));
            BorderPane borderPane = new BorderPane();
            borderPane.setCenter(canvas);
            // borderPane.setCenter(imageView);
            borderPane.setBottom(filenameLabel);
            BorderPane.setAlignment(filenameLabel, javafx.geometry.Pos.CENTER);
            BorderPane.setMargin(canvas, new Insets(5));
            // BorderPane.setMargin(imageView, new Insets(5));

            ImageContainer imageContainer = new ImageContainer(filenameLabel, borderPane);
            imageContainer.setFileImagePath(imagePath);

            StackPane stackPane = new StackPane();
            stackPane.getChildren().addAll(imageContainer.getHighlightRect(), imageContainer.getContainer());
            stackPane.setOnMouseClicked(event -> {
                try {
                    pauseSelectByFocus = true;
                    stackPane.requestFocus();
                    selectImageContainer(imageContainer);
                } finally {
                    pauseSelectByFocus = false;
                }
            });
            stackPane.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (pauseSelectByFocus) {
                    return;
                }
                if (newValue != null && newValue && selectedImageContainer != imageContainer) {
                    selectImageContainer(imageContainer);
                }
            });
            stackPane.setFocusTraversable(true);
            containerHolders.add(imageContainer);
            imageContainer.setMainPane(stackPane);
            flowPaneChildren.add(stackPane);
        }
        executorService.shutdown();

        ScrollPane scrollPane = new ScrollPane(flowPane);
        this.scrollPane = scrollPane;
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFocusTraversable(false);
        scrollPane.setOnKeyPressed(this::handleKeyPress);
        galleryPane.getChildren().addAll(scrollPane);
        return galleryPane;
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
            if (selectedImageContainer != null) {
                selectedImageContainer.getHighlightRect().setVisible(false);
            }
            if (selectedImageContainer != imageContainer) {
                imageContainer.getHighlightRect().setVisible(true);
                selectedImageContainer = imageContainer;
                String nextFileImagePath = getNextFileImagePath();
                String fileImagePath = imageContainer.getFileImagePath();
                setLastAccessTime(Path.of(fileImagePath));
                MyController.getInstance().getProjectionScreenController().setImage(fileImagePath, ProjectionType.IMAGE, nextFileImagePath);
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

    private String getNextFileImagePath() {
        int selectedIndex = getSelectedImageIndex();
        ImageContainer nextImageContainer = getImageContainer(selectedIndex + 1);
        String nextFileImagePath;
        if (nextImageContainer != null) {
            nextFileImagePath = nextImageContainer.getFileImagePath();
        } else {
            nextFileImagePath = null;
        }
        return nextFileImagePath;
    }

    private List<String> getImagePathsFromFolder(@SuppressWarnings("SameParameterValue") String folderPath) {
        List<String> imagePaths = new ArrayList<>();
        File folder = new File(folderPath);
        List<File> orderedFiles = new ArrayList<>();
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (isImageFile(file.getName())) {
                        orderedFiles.add(file);
                    }
                }
            }
        }
        orderedFiles.sort(getComparator());
        for (File file : orderedFiles) {
            imagePaths.add(file.getAbsolutePath());
        }
        return imagePaths;
    }

    private static Comparator<File> getComparator() {
        ImageOrderMethod imageOrderMethod = settings.getImageOrderMethod();
        return switch (imageOrderMethod) {
            case ASCENDING_BY_TITLE -> Comparator.comparing(File::getName);
            case DESCENDING_BY_TITLE -> (o1, o2) -> o2.getName().compareTo(o1.getName());
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
    }

    private boolean isImageFile(String fileName) {
        String[] imageExtensions = {".jpg", ".jpeg", ".png", ".gif"};
        for (String extension : imageExtensions) {
            if (fileName.toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
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

    private int getNumImages() {
        return flowPane.getChildren().size();
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
            if (imageContainer != null) {
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
        if (selectedIndex < getNumImages() - 1) {
            setSelectedImage(selectedIndex + 1);
        }
    }

    public void setPrevious() {
        int selectedIndex = getSelectedImageIndex();
        if (selectedIndex > 0) {
            setSelectedImage(selectedIndex - 1);
        }
    }

    private record Result(BorderPane borderPane, TextField valueTextField, Slider slider) {


    }

}
