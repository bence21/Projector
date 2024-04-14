package projector.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.controller.util.ProjectionScreenHolder;
import projector.model.CustomCanvas;
import projector.service.CustomCanvasService;
import projector.ui.NumberTextField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

import static projector.controller.MyController.calculateSizeByScale;
import static projector.controller.MyController.scaleByPrimaryScreen;
import static projector.controller.util.ControllerUtil.getFxmlLoader;
import static projector.controller.util.ControllerUtil.getStageWithRoot;

public class CustomCanvasesController {

    private static final Logger LOG = LoggerFactory.getLogger(CustomCanvasesController.class);
    public VBox vBox;
    private ArrayList<CustomCanvas> customCanvases;
    private final String CUSTOM_CANVAS = "Custom Canvas";
    private final String SEPARATOR = " - ";

    public void initialize() {
        customCanvases = CustomCanvasService.getInstance().getCustomCanvases();
        for (CustomCanvas customCanvas : customCanvases) {
            addCustomCanvas(customCanvas);
        }
    }

    public void onAddCustomCanvas() {
        CustomCanvas customCanvas = new CustomCanvas();
        customCanvas.apply(getLastCustomCanvas());
        String s = CUSTOM_CANVAS;
        int size = customCanvases.size();
        if (size > 0) {
            s += SEPARATOR + getNextNumber(customCanvases, size + 1);
        }
        customCanvas.setName(s);
        copyLastScreenSettings(customCanvases, customCanvas);
        MyController.getInstance().createCustomCanvasStage(customCanvas);
        addCustomCanvas(customCanvas);
        customCanvases.add(customCanvas);
    }

    private void copyLastScreenSettings(ArrayList<CustomCanvas> customCanvases, CustomCanvas customCanvas) {
        int size = customCanvases.size();
        if (size == 0) {
            return;
        }
        CustomCanvas lastCustomCanvas = customCanvases.get(size - 1);
        ProjectionScreenHolder projectionScreenHolder = lastCustomCanvas.getProjectionScreenHolder();
        if (projectionScreenHolder == null) {
            return;
        }
        projectionScreenHolder.getProjectionScreenSettings().copyTo(customCanvas.getName());
    }

    private int getNextNumber(ArrayList<CustomCanvas> customCanvases, int k) {
        HashMap<String, Boolean> hashMap = new HashMap<>();
        for (CustomCanvas customCanvas : customCanvases) {
            hashMap.put(customCanvas.getName(), true);
        }
        String s;
        do {
            s = CUSTOM_CANVAS + SEPARATOR + k++;
        } while (hashMap.containsKey(s));
        return --k;
    }

    private CustomCanvas getLastCustomCanvas() {
        if (customCanvases.isEmpty()) {
            return CustomCanvasService.getInstance().getCustomCanvasFromOldSettings();
        }
        return customCanvases.get(customCanvases.size() - 1);
    }

    private void addCustomCanvas(CustomCanvas customCanvas) {
        ObservableList<Node> vBoxChildren = vBox.getChildren();
        HBox hBox = new HBox();
        hBox.setAlignment(javafx.geometry.Pos.CENTER);
        hBox.setSpacing(10);
        hBox.setPadding(new Insets(10, 10, 0, 10));
        ObservableList<Node> hBoxChildren = hBox.getChildren();
        addNameTextField(customCanvas, hBoxChildren);
        Stage stage = customCanvas.getStage();
        customCanvas.setPositionX(scaleByPrimaryScreen(stage.getX()));
        customCanvas.setPositionY(scaleByPrimaryScreen(stage.getY()));
        addDoubleTextField(hBoxChildren, customCanvas::setWidth, customCanvas::getWidth, "Width:", stage::setWidth);
        addDoubleTextField(hBoxChildren, customCanvas::setHeight, customCanvas::getHeight, "Height:", stage::setHeight);
        addDoubleTextField(hBoxChildren, customCanvas::setPositionX, customCanvas::getPositionX, "X:", stage::setX);
        addDoubleTextField(hBoxChildren, customCanvas::setPositionY, customCanvas::getPositionY, "Y:", stage::setY);
        addRemoveButton(hBoxChildren, customCanvas, vBoxChildren, hBox);
        vBoxChildren.add(hBox);
    }

    private void addRemoveButton(ObservableList<Node> hBoxChildren, CustomCanvas customCanvas, ObservableList<Node> vBoxChildren, HBox hBox) {
        Button button = new Button("Remove");
        button.setOnAction(event -> {
            vBoxChildren.remove(hBox);
            customCanvases.remove(customCanvas);
            customCanvas.close();
        });
        hBoxChildren.add(button);
    }

    private static void addNameTextField(CustomCanvas customCanvas, ObservableList<Node> hBoxChildren) {
        TextField nameTextField = new TextField();
        nameTextField.setText(customCanvas.getName());
        nameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            customCanvas.setName(newValue);
            Stage stage = customCanvas.getStage();
            if (stage != null) {
                stage.setTitle(newValue);
            }
        });
        hBoxChildren.add(nameTextField);
    }

    private void addDoubleTextField(ObservableList<Node> hBoxChildren, DoubleConsumer doubleConsumer, DoubleSupplier doubleSupplier, String labelText, DoubleConsumer stageDoubleConsumer) {
        Label label = new Label(labelText);
        hBoxChildren.add(label);
        NumberTextField textField = new NumberTextField();
        textField.setPrefWidth(55.0);
        setTextToTextField(doubleSupplier, textField);
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            Double aDouble = getDouble(newValue);
            if (aDouble != null) {
                doubleConsumer.accept(aDouble);
                aDouble = calculateSizeByScale(aDouble);
                stageDoubleConsumer.accept(aDouble);
            }
        });
        hBoxChildren.add(textField);
    }

    private static void setTextToTextField(DoubleSupplier doubleSupplier, NumberTextField textField) {
        textField.setValue(doubleSupplier.getAsDouble());
    }

    private static Double getDouble(String newValue) {
        try {
            return Double.parseDouble(newValue);
        } catch (Exception e) {
            return null;
        }
    }

    public static CustomCanvasesController openCustomCanvases(Class<?> aClass) {
        try {
            FXMLLoader loader = getFxmlLoader("CustomCanvases");
            Pane root = loader.load();
            CustomCanvasesController controller = loader.getController();
            Stage stage = getStageWithRoot(aClass, root);
            stage.setTitle("Custom Canvases");
            stage.setOnCloseRequest(event -> CustomCanvasService.getInstance().save());
            stage.show();
            return controller;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }
}
