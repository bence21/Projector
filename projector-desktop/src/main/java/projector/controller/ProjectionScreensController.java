package projector.controller;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import projector.application.ProjectionType;
import projector.application.Settings;
import projector.controller.listener.ProjectionScreenListener;
import projector.controller.util.ProjectionScreenHolder;
import projector.controller.util.ProjectionScreensUtil;
import projector.utils.ImageUtil;
import projector.utils.monitors.Monitor;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static projector.controller.CustomCanvasesController.openCustomCanvases;
import static projector.controller.MyController.calculateSizeByScale;
import static projector.controller.ProjectionTypeController.openProjectionType;
import static projector.utils.SceneUtils.getCustomStage;

public class ProjectionScreensController {

    private final List<Bunch> bunches = new ArrayList<>();
    @FXML
    private VBox vBox;
    private boolean initialized = false;
    private Tab projectionScreensTab;
    private final double BUTTON_SIZE = 20.0;

    private static int minimumSize(int size) {
        if (size > 0) {
            return size;
        }
        return 1;
    }

    public static double getScreenScale(ProjectionScreenHolder projectionScreenHolder, Screen screen) {
        if (screen == null) {
            return 1;
        }
        GraphicsDevice[] screenDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        double mainScreenWidth = screen.getBounds().getWidth();
        Integer index = projectionScreenHolder.getScreenIndex();
        if (index == null) {
            return 1;
        }
        if (index < 0 || index >= screenDevices.length) {
            return 1;
        }
        GraphicsDevice screenDevice = screenDevices[index];
        DisplayMode displayMode = screenDevice.getDisplayMode();
        int mainScreenTrueWidth = displayMode.getWidth();
        return mainScreenTrueWidth / mainScreenWidth;
    }

    private ImageView getImageView(ProjectionScreenHolder projectionScreenHolder) {
        ImageView imageView = ImageUtil.getImageView(400, 200);
        ProjectionScreenController projectionScreenController = projectionScreenHolder.getProjectionScreenController();
        BorderPane mainPane = projectionScreenController.getMainPane();
        Bunch bunch = new Bunch();
        bunch.mainPane = mainPane;
        bunch.imageView = imageView;
        bunch.projectionScreenHolder = projectionScreenHolder;
        projectionScreenHolder.setBunch(bunch);
        snapshot(bunch);
        bunches.add(bunch);
        imageView.setOnMouseClicked(event -> snapshot(bunch));
        projectionScreenController.addViewChangedListener(() -> snapshot(bunch));
        return imageView;
    }

    private void snapshot(Bunch bunch) {
        if (!projectionScreensTab.isSelected()) {
            return;
        }
        BorderPane mainPane = bunch.mainPane;
        ImageView imageView = bunch.imageView;
        if (mainPane == null || imageView == null) {
            return;
        }
        Parent parent = mainPane.getParent();
        if (parent == null) {
            return;
        }
        if (!(parent instanceof BorderPane parentPane)) {
            return;
        }
        Scene scene = parentPane.getScene();
        if (scene != null && scene.getFill() == null) {
            scene.setFill(Color.TRANSPARENT);
        }
        WritableImage writableImage = getWritableImage(bunch, parentPane);
        parentPane.snapshot(null, writableImage);
        imageView.setImage(writableImage);
    }

    private static WritableImage getWritableImage(Bunch bunch, BorderPane parentPane) {
        double parentPaneWidth = parentPane.getWidth();
        double parentPaneHeight = parentPane.getHeight();
        ProjectionScreenHolder projectionScreenHolder = bunch.projectionScreenHolder;
        projectionScreenHolder.onSizeChanged(parentPaneWidth, parentPaneHeight);
        int width = (int) parentPaneWidth;
        int height = (int) parentPaneHeight;
        width = minimumSize(width);
        height = minimumSize(height);
        return new WritableImage(width, height);
    }

    public void lazyInitialize(Tab projectionScreensTab) {
        this.projectionScreensTab = projectionScreensTab;
        if (this.initialized) {
            return;
        }
        this.initialized = true;
        initializeOnProjectionScreensTabSelection(projectionScreensTab);
        ProjectionScreensUtil projectionScreensUtil = ProjectionScreensUtil.getInstance();
        List<ProjectionScreenHolder> projectionScreenHolders = projectionScreensUtil.getProjectionScreenHolders();
        vBox.getChildren().clear();
        addToolHBox();
        for (ProjectionScreenHolder projectionScreenHolder : projectionScreenHolders) {
            addProjectionScreenHolderToVBox(projectionScreenHolder);
        }
        projectionScreensUtil.addProjectionScreenListener(new ProjectionScreenListener() {
            @Override
            public void onNew(ProjectionScreenHolder projectionScreenHolder) {
                addProjectionScreenHolderToVBox(projectionScreenHolder);
            }

            @Override
            public void onRemoved(ProjectionScreenHolder projectionScreenHolder) {
                removeProjectionScreenHolderFromVBox(projectionScreenHolder);
            }
        });
    }

    private void addToolHBox() {
        vBox.getChildren().add(getToolHBox());
    }

    private HBox getToolHBox() {
        HBox toolHBox = new HBox();
        toolHBox.setSpacing(10);
        toolHBox.setPadding(new Insets(10, 10, 0, 10));
        Button customCanvasesButton = new Button("Custom canvases");
        customCanvasesButton.setOnAction((event) -> openCustomCanvases(getClass()));
        toolHBox.getChildren().addAll(customCanvasesButton);
        return toolHBox;
    }

    private void removeProjectionScreenHolderFromVBox(ProjectionScreenHolder projectionScreenHolder) {
        vBox.getChildren().remove(projectionScreenHolder.getHBox());
    }

    private void initializeOnProjectionScreensTabSelection(Tab projectionScreensTab) {
        projectionScreensTab.setOnSelectionChanged(event -> {
            if (projectionScreensTab.isSelected()) {
                for (Bunch bunch : bunches) {
                    snapshot(bunch);
                }
            }
        });
    }

    private void addProjectionScreenHolderToVBox(ProjectionScreenHolder projectionScreenHolder) {
        ObservableList<Node> vBoxChildren = vBox.getChildren();
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        hBox.setPrefHeight(50.0);
        hBox.setSpacing(10.0);
        VBox vBoxLeft = new VBox();
        vBoxLeft.setAlignment(Pos.CENTER);
        HBox hBoxLeft = new HBox();
        hBoxLeft.setAlignment(Pos.CENTER);
        hBoxLeft.setPrefHeight(50.0);
        hBoxLeft.setSpacing(10.0);
        ObservableList<Node> vBoxLeftChildren = vBoxLeft.getChildren();
        vBoxLeftChildren.add(hBoxLeft);
        ObservableList<Node> hBoxLeftChildren = hBoxLeft.getChildren();
        addLabel(projectionScreenHolder, hBoxLeftChildren);
        ResourceBundle resourceBundle = Settings.getInstance().getResourceBundle();
        Button settingsButton = getSettingsButton(projectionScreenHolder, resourceBundle);
        hBoxLeftChildren.add(settingsButton);
        hBoxLeftChildren.add(getProjectionTypeButton(projectionScreenHolder, resourceBundle));
        ProjectionScreenController projectionScreenController = projectionScreenHolder.getProjectionScreenController();
        hBoxLeftChildren.add(getBlankButton(resourceBundle, projectionScreenController));
        hBoxLeftChildren.add(getClearButton(resourceBundle, projectionScreenController));
        hBoxLeftChildren.add(getLockButton(resourceBundle, projectionScreenController));
        addShowProjectionScreenButton(projectionScreenHolder, hBoxLeftChildren, projectionScreenController);
        ObservableList<Node> hBoxChildren = hBox.getChildren();
        hBoxChildren.add(vBoxLeft);
        hBoxChildren.add(getImageView(projectionScreenHolder));
        projectionScreenHolder.setHBox(hBox);
        vBoxChildren.add(hBox);
        vBoxLeftChildren.add(getSecondRow(projectionScreenHolder));
    }

    private void addLabel(ProjectionScreenHolder projectionScreenHolder, ObservableList<Node> hBoxChildren) {
        Label label = new Label();
        setLabelTextByHolder(label, projectionScreenHolder);
        projectionScreenHolder.addOnNameChangeListener(() -> setLabelTextByHolder(label, projectionScreenHolder));
        HBox.setMargin(label, new Insets(0, 0, 0, 12));
        hBoxChildren.add(label);
    }

    private static void setLabelTextByHolder(Label label, ProjectionScreenHolder projectionScreenHolder) {
        label.setText(projectionScreenHolder.getProjectionScreenSettings().getName());
    }

    private Button getSettingsButton(ProjectionScreenHolder projectionScreenHolder, ResourceBundle resourceBundle) {
        Button settingsButton = getButton();
        settingsButton.setText(resourceBundle.getString("Settings"));
        settingsButton.setOnAction(onSettingsAction(projectionScreenHolder));
        return settingsButton;
    }

    private Button getButton() {
        Button button = new Button();
        button.setPrefHeight(BUTTON_SIZE);
        button.setPadding(new Insets(4.0));
        return button;
    }

    private Button getProjectionTypeButton(ProjectionScreenHolder projectionScreenHolder, ResourceBundle resourceBundle) {
        Button projectionTypeButton = getButton();
        projectionTypeButton.setText(resourceBundle.getString("Projection type"));
        projectionTypeButton.setOnAction(onProjectionTypeAction(projectionScreenHolder));
        return projectionTypeButton;
    }

    private EventHandler<ActionEvent> onProjectionTypeAction(ProjectionScreenHolder projectionScreenHolder) {
        return new EventHandler<>() {
            @Override
            public void handle(ActionEvent event) {
                openProjectionType(getClass(), projectionScreenHolder);
            }
        };
    }

    private ToggleButton getBlankButton(ResourceBundle resourceBundle, ProjectionScreenController projectionScreenController) {
        ToggleButton blankButton = new ToggleButton();
        blankButton.setMnemonicParsing(false);
        blankButton.setText(resourceBundle.getString("Black"));
        HBox.setMargin(blankButton, new Insets(2, 4, 0, 0));
        blankButton.setContentDisplay(ContentDisplay.CENTER);
        blankButton.setGraphicTextGap(0.0);
        blankButton.setPrefHeight(BUTTON_SIZE);
        blankButton.setTextAlignment(TextAlignment.CENTER);
        blankButton.setPadding(new Insets(4.0));
        projectionScreenController.addOnBlankListener(blankButton::setSelected);
        blankButton.setOnAction(event -> projectionScreenController.toggleBlank());
        return blankButton;
    }

    private Button getClearButton(ResourceBundle resourceBundle, ProjectionScreenController projectionScreenController) {
        Button clearButton = new Button();
        clearButton.setText(resourceBundle.getString("Clear"));
        HBox.setMargin(clearButton, new Insets(2, 4, 0, 0));
        clearButton.setContentDisplay(ContentDisplay.CENTER);
        clearButton.setGraphicTextGap(0.0);
        clearButton.setPrefHeight(BUTTON_SIZE);
        clearButton.setTextAlignment(TextAlignment.CENTER);
        clearButton.setPadding(new Insets(4.0));
        clearButton.setOnAction(event -> projectionScreenController.clearAll(ProjectionType.CLEAR));
        return clearButton;
    }

    private ToggleButton getLockButton(ResourceBundle resourceBundle, ProjectionScreenController projectionScreenController) {
        ToggleButton lockButton = new ToggleButton();
        lockButton.setMnemonicParsing(false);
        lockButton.setText(resourceBundle.getString("Lock"));
        HBox.setMargin(lockButton, new Insets(2, 4, 0, 0));
        lockButton.setContentDisplay(ContentDisplay.CENTER);
        lockButton.setGraphicTextGap(0.0);
        lockButton.setPrefHeight(BUTTON_SIZE);
        lockButton.setTextAlignment(TextAlignment.CENTER);
        lockButton.setPadding(new Insets(4.0));
        projectionScreenController.addOnLockListener(lockButton::setSelected);
        lockButton.setOnAction(event -> projectionScreenController.toggleLock());
        return lockButton;
    }

    private void addShowProjectionScreenButton(ProjectionScreenHolder projectionScreenHolder, ObservableList<Node> hBoxChildren, ProjectionScreenController projectionScreenController) {
        ToggleButton showProjectionScreenToggleButton = getShowProjectionScreenToggleButton(projectionScreenHolder);
        hBoxChildren.add(showProjectionScreenToggleButton);
        showProjectionScreenToggleButton.managedProperty().bind(showProjectionScreenToggleButton.visibleProperty());
        Popup popup = projectionScreenController.getPopup();
        showProjectionScreenToggleButton.setVisible(popup != null);
        if (popup == null) {
            projectionScreenHolder.setOnPopupCreatedListener(() -> showProjectionScreenToggleButton.setVisible(true));
        }
    }

    private HBox getSecondRow(ProjectionScreenHolder projectionScreenHolder) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        hBox.setPrefHeight(50.0);
        hBox.setSpacing(10.0);
        Label sizeLabel = new Label();
        setLabelTextByHolder(sizeLabel, projectionScreenHolder);
        projectionScreenHolder.setOnMainPaneSizeChangeListener((width, height) -> {
            MonitorSizes monitorSizes = getMonitorSizes(projectionScreenHolder, width, height);
            sizeLabel.setText("(" + (int) (Math.round(monitorSizes.width())) + " x " + (int) (Math.round(monitorSizes.height())) + ")");
        });
        HBox.setMargin(sizeLabel, new Insets(0, 0, 0, 12));
        ObservableList<Node> hBoxChildren = hBox.getChildren();
        hBoxChildren.add(sizeLabel);
        return hBox;
    }

    private static MonitorSizes getMonitorSizes(ProjectionScreenHolder projectionScreenHolder, double width, double height) {
        ProjectionScreenController projectionScreenController = projectionScreenHolder.getProjectionScreenController();
        if (projectionScreenController != null) {
            Monitor monitor = projectionScreenController.getMonitor();
            if (monitor != null) {
                width = monitor.getWidth();
                height = monitor.getHeight();
            } else {
                Screen screen = projectionScreenController.getScreen();
                if (screen != null) {
                    double scale = getScreenScale(projectionScreenHolder, screen);
                    width = width * scale;
                    height = height * scale;
                }
            }
        }
        return new MonitorSizes(width, height);
    }

    private record MonitorSizes(double width, double height) {
    }

    private ToggleButton getShowProjectionScreenToggleButton(ProjectionScreenHolder projectionScreenHolder) {
        ToggleButton toggleButton = new ToggleButton();
        toggleButton.setContentDisplay(ContentDisplay.CENTER);
        toggleButton.setFocusTraversable(false);
        toggleButton.setGraphicTextGap(0.0);
        double size = 19.0;
        toggleButton.setPrefHeight(size);
        toggleButton.setPrefWidth(size);
        toggleButton.setSelected(true);
        toggleButton.setTextAlignment(TextAlignment.CENTER);
        toggleButton.setPadding(new Insets(4.0));
        projectionScreenHolder.setOnProjectionToggle(() -> onProjectionToggle(projectionScreenHolder));
        toggleButton.setOnAction(event -> {
            projectionScreenHolder.getProjectionScreenController().toggleShowHidePopup();
            onProjectionToggle(projectionScreenHolder);
        });
        ImageView imageView = new ImageView();
        imageView.setFitHeight(size);
        imageView.setFitWidth(size);
        imageView.setPickOnBounds(true);
        imageView.setPreserveRatio(true);
        InputStream resourceAsStream = getClass().getResourceAsStream("/icons/monitor.png");
        if (resourceAsStream != null) {
            imageView.setImage(new Image(resourceAsStream));
            toggleButton.setGraphic(imageView);
        }
        return toggleButton;
    }

    private void onProjectionToggle(ProjectionScreenHolder projectionScreenHolder) {
        Monitor monitor = projectionScreenHolder.getProjectionScreenController().getMonitor();
        ImageView imageView = projectionScreenHolder.getBunch().imageView;
        if (monitor == null && imageView.getOpacity() >= 1.0) {
            return;
        }
        imageView.setOpacity(getOpacityByMonitorProjection(projectionScreenHolder, monitor));
    }

    private double getOpacityByMonitorProjection(ProjectionScreenHolder projectionScreenHolder, Monitor monitor) {
        if (projectionScreenHolder.isPopupShowing() || monitor == null) {
            return 1.0;
        } else {
            return 0.5;
        }
    }

    private EventHandler<ActionEvent> onSettingsAction(ProjectionScreenHolder projectionScreenHolder) {
        return new EventHandler<>() {
            @Override
            public void handle(ActionEvent event) {
                ResourceBundle resourceBundle = Settings.getInstance().getResourceBundle();
                String title = resourceBundle.getString("Settings") + " - " + projectionScreenHolder.getProjectionScreenSettings().getName();
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/view/ProjectionScreenSettings.fxml"));
                loader.setResources(resourceBundle);
                try {
                    Pane root = loader.load();
                    GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
                    int height = gd.getDisplayMode().getHeight();
                    Scene scene = new Scene(root);
                    Stage settingsStage = getCustomStage(getClass(), scene);
                    settingsStage.setWidth(743);
                    settingsStage.setHeight(calculateSizeByScale(height - 100));
                    settingsStage.setTitle(title);
                    ProjectionScreenSettingsController settingsController = loader.getController();
                    settingsController.setProjectionScreenHolder(projectionScreenHolder);
                    settingsController.setStage(settingsStage);
                    settingsStage.show();
                } catch (IOException ignored) {
                }
            }
        };
    }

    public static class Bunch {
        BorderPane mainPane;
        ImageView imageView;
        ProjectionScreenHolder projectionScreenHolder;
    }
}
