package projector.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.PTextAlignment;
import projector.application.ProjectionScreenSettings;
import projector.application.Settings;
import projector.controller.util.ProjectionScreenBunch;
import projector.controller.util.ProjectionScreenHolder;
import projector.ui.NumberTextField;
import projector.ui.ResetButton;

import java.util.function.DoubleConsumer;

import static projector.controller.SettingsController.addFonts;
import static projector.controller.SettingsController.getDoubleFromTextField;
import static projector.controller.SettingsController.getFontWeightByString;
import static projector.controller.SettingsController.getProgressLineSpinnerValueFactory;
import static projector.controller.SettingsController.getStrokeSizeFactory;
import static projector.controller.SettingsController.imageBrowseWithTextFieldResult;
import static projector.controller.SettingsController.initializeStrokeTypeComboBox_;
import static projector.controller.UtilsController.handleProjectionScreensWithScreenComboBox;
import static projector.utils.NumberUtil.getIntegerFromNumber;

public class ProjectionScreenSettingsController {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectionScreenSettingsController.class);
    public BorderPane mainBorderPain;
    public ScrollPane scrollPane;
    public Slider maxFontSlider;
    public CheckBox breakLinesCheckbox;
    public Slider breakAfterSlider;
    public Slider lineSpaceSlider;
    public CheckBox showSongSecondTextCheckBox;
    public ColorPicker songSecondTextColorPicker;
    public ListView<Text> fontListView;
    public ColorPicker colorPicker;
    public RadioButton imageRadioButton;
    public TextField imagePathTextField;
    public Button imageBrowseButton;
    public RadioButton colorRadioButton;
    public ColorPicker backgroundColorPicker;
    public ColorPicker progressLineColorPicker;
    public Spinner<Integer> progressLineThicknessSpinner;
    public RadioButton progressLinePositionTopRadioButton;
    public RadioButton progressLinePositionBottomRadioButton;
    public ComboBox<String> fontWeightComboBox;
    public ResetButton colorReset;
    public ResetButton backgroundColorReset;
    public ResetButton maxFontSliderReset;
    public ResetButton breakLinesCheckboxReset;
    public ResetButton breakAfterSliderReset;
    public ResetButton lineSpaceSliderReset;
    public ResetButton imageRadioButtonReset;
    public ResetButton colorRadioButtonReset;
    public ResetButton progressLineColorPickerReset;
    public ResetButton progressLineThicknessSpinnerReset;
    public ResetButton progressLinePositionReset;
    public ResetButton fontWeightComboBoxReset;
    public ResetButton showSongSecondTextCheckBoxReset;
    public ResetButton songSecondTextColorPickerReset;
    public ResetButton fontListViewReset;
    public ResetButton imageBrowseButtonReset;
    public CheckBox strokeCheckbox;
    public ResetButton strokeCheckboxReset;
    public ColorPicker strokeColorPicker;
    public ResetButton strokeColorPickerReset;
    public Spinner<Double> strokeSizeSpinner;
    public ResetButton strokeSizeSpinnerReset;
    public ComboBox<StrokeType> strokeTypeComboBox;
    public ResetButton strokeTypeComboBoxReset;
    // public MyTextFlow textFlow;
    public ToggleButton liveButton;
    public Slider verticalAlignmentSlider;
    public ResetButton verticalAlignmentSliderReset;
    public Slider horizontalAlignmentSlider;
    public ResetButton horizontalAlignmentSliderReset;
    public ComboBox<PTextAlignment> textAlignmentComboBox;
    public ResetButton textAlignmentComboBoxReset;
    public NumberTextField topMarginTextField;
    public NumberTextField rightMarginTextField;
    public NumberTextField bottomMarginTextField;
    public NumberTextField leftMarginTextField;
    public CheckBox asPaddingCheckbox;
    public ResetButton marginsReset;
    public ComboBox<ProjectionScreenBunch> screenComboBox;
    public HBox swapScreenHBox;
    public TextField nameTextField;
    public CheckBox progressBarCheckbox;
    public Slider progressBarHeightSlider;
    public CheckBox nextSectionCheckbox;
    public Slider nextSectionHeightSlider;
    private Stage stage;
    private ProjectionScreenSettings projectionScreenSettings;
    private ProjectionScreenSettings projectionScreenSettingsModel;
    private ProjectionScreenHolder projectionScreenHolder;

    public void onImageBrowseButtonAction() {
        imageBrowseWithTextFieldResult(imagePathTextField);
    }

    public void onSaveButtonAction() {
        projectionScreenSettings.setMaxFont(projectionScreenSettingsModel.getMaxFont());
        projectionScreenSettings.setBreakLines(projectionScreenSettingsModel.getBreakLines());
        projectionScreenSettings.setBreakAfter(projectionScreenSettingsModel.getBreakAfter());
        projectionScreenSettings.setLineSpace(projectionScreenSettingsModel.getLineSpace());
        projectionScreenSettings.setColor(projectionScreenSettingsModel.getColor());
        projectionScreenSettings.setIsBackgroundImage(projectionScreenSettingsModel.getIsBackgroundImage());
        projectionScreenSettings.setBackgroundImagePath(projectionScreenSettingsModel.getBackgroundImagePath());
        projectionScreenSettings.setBackgroundColor(projectionScreenSettingsModel.getBackgroundColor());
        projectionScreenSettings.setProgressLineColor(projectionScreenSettingsModel.getProgressLineColor());
        projectionScreenSettings.setProgressLineThickness(projectionScreenSettingsModel.getProgressLineThickness());
        projectionScreenSettings.setProgressLinePositionIsTop(projectionScreenSettingsModel.getProgressLinePosition());
        projectionScreenSettings.setFontWeight(projectionScreenSettingsModel.getFontWeightString());
        projectionScreenSettings.setStrokeFont(projectionScreenSettingsModel.getStrokeFont());
        projectionScreenSettings.setStrokeColor(projectionScreenSettingsModel.getStrokeColor());
        projectionScreenSettings.setStrokeSize(projectionScreenSettingsModel.getStrokeSize());
        projectionScreenSettings.setStrokeType(projectionScreenSettingsModel.getStrokeType());
        projectionScreenSettings.setShowSongSecondText(projectionScreenSettingsModel.getShowSongSecondText());
        projectionScreenSettings.setSongSecondTextColor(projectionScreenSettingsModel.getSongSecondTextColor());
        projectionScreenSettings.setFont(projectionScreenSettingsModel.getFont());
        projectionScreenSettings.setVerticalAlignment(projectionScreenSettingsModel.getVerticalAlignment());
        projectionScreenSettings.setHorizontalAlignment(projectionScreenSettingsModel.getHorizontalAlignment());
        projectionScreenSettings.setTextAlignment(projectionScreenSettingsModel.getTextAlignment());
        projectionScreenSettings.setTopMargin(projectionScreenSettingsModel.getTopMargin());
        projectionScreenSettings.setRightMargin(projectionScreenSettingsModel.getRightMargin());
        projectionScreenSettings.setBottomMargin(projectionScreenSettingsModel.getBottomMargin());
        projectionScreenSettings.setLeftMargin(projectionScreenSettingsModel.getLeftMargin());
        projectionScreenSettings.setAsPadding(projectionScreenSettingsModel.getAsPadding());
        projectionScreenSettings.setProgressBar(projectionScreenSettingsModel.getProgressBar());
        projectionScreenSettings.setProgressBarHeight(projectionScreenSettingsModel.getProgressBarHeight());
        projectionScreenSettings.setNextSection(projectionScreenSettingsModel.getNextSection());
        projectionScreenSettings.setNextSectionHeight(projectionScreenSettingsModel.getNextSectionHeight());
        projectionScreenSettings.setName(projectionScreenSettingsModel.getName_());
        projectionScreenSettings.save();
        projectionScreenHolder.onNameChanged();
        ProjectionScreenController projectionScreenController = projectionScreenHolder.getProjectionScreenController();
        if (projectionScreenController != null) {
            projectionScreenController.setProjectionScreenSettings(projectionScreenSettings);
            projectionScreenController.onSettingsChanged();
        }
        stage.close();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setProjectionScreenHolder(ProjectionScreenHolder projectionScreenHolder) {
        this.projectionScreenHolder = projectionScreenHolder;
        projectionScreenSettings = projectionScreenHolder.getProjectionScreenSettings();
        projectionScreenSettingsModel = new ProjectionScreenSettings(projectionScreenSettings);
        projectionScreenSettingsModel.setUseGlobalSettings(false);
        maxFontSlider.setValue(projectionScreenSettings.getMaxFont());
        boolean breakLines = projectionScreenSettings.isBreakLines();
        breakLinesCheckbox.setSelected(breakLines);
        breakAfterSlider.setDisable(breakLines);
        breakAfterSlider.setValue(projectionScreenSettings.getBreakAfter());
        breakLinesCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> breakAfterSlider.setDisable(newValue));
        addAndSelectFirstFont(projectionScreenSettings.getFont());
        fontListView.getSelectionModel().selectedItemProperty().addListener(this::changed);
        lineSpaceSlider.setMax(10);
        lineSpaceSlider.valueChangingProperty().addListener(this::changed);
        initializeProgressLine();
        Settings settings = Settings.getInstance();
        initializeMaxFontSlider(settings);
        initializeBreakLinesCheckbox(settings);
        initializeBreakAfterSlider(settings);
        initializeLineSpaceSlider(settings);
        initializeColorPicker(settings);
        initializeBackgroundRadio();
        initializeImageRadioButton();
        initializeColorRadioButton();
        initializeBackgroundImagePath(settings);
        initializeBackgroundColorPicker(settings);
        initializeProgressLineColorPicker(settings);
        initializeProgressLineThicknessSpinner(settings);
        initializeProgressLinePosition(settings);
        initializeFontWeightComboBox(settings);
        initializeStrokeFontCheckBox(settings);
        initializeStrokeColorCheckBox(settings);
        initializeStrokeSizeSpinner(settings);
        initializeStrokeTypeComboBox(settings);
        initializeVerticalAlignmentSlider(settings);
        initializeHorizontalAlignmentSlider(settings);
        initializeTextAlignment(settings);
        initializeMargins(settings);
        initializeShowSongSecondTextCheckBox(settings);
        initializeSongSecondTextColorPicker(settings);
        initializeFontListView(settings);
        initializeProgressBar();
        initializeNextSection();
        showSongSecondTextCheckBox.setSelected(projectionScreenSettings.isShowSongSecondText());
        songSecondTextColorPicker.setValue(projectionScreenSettings.getSongSecondTextColor());
        projectionScreenSettingsModel.setOnChangedListener(this::updatePreview);
        // textFlow.setProjectionScreenSettings(projectionScreenSettingsModel);
        // previewPane.widthProperty().addListener(getChangeListener());
        // previewPane.heightProperty().addListener(getChangeListener());
        initializeScreenComboBox();
        updatePreview();
    }

    private void initializeScreenComboBox() {
        try {
            nameTextField.setText(projectionScreenSettings.getName());
            nameTextField.textProperty().addListener((observableValue, oldValue, newValue) -> projectionScreenSettingsModel.setName(newValue));
            handleProjectionScreensWithScreenComboBox(screenComboBox, projectionScreenHolder);
            screenComboBox.getSelectionModel().selectedItemProperty().addListener((o, old, newValue) -> swapProjectionScreen(projectionScreenHolder, newValue.getProjectionScreenHolder()));
            if (screenComboBox.getItems().size() == 0) {
                swapScreenHBox.setVisible(false); // parent container is important for styleClass
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void swapProjectionScreen(ProjectionScreenHolder projectionScreenHolder1, ProjectionScreenHolder projectionScreenHolder2) {
        try {
            String temp = projectionScreenHolder1.getName();
            String temporarySwappingScreen = "TemporarySwappingScreen";
            ProjectionScreenSettings projectionScreenSettings1 = projectionScreenHolder1.getProjectionScreenSettings();
            ProjectionScreenSettings projectionScreenSettings2 = projectionScreenHolder2.getProjectionScreenSettings();
            String settings1Name_ = projectionScreenSettings1.getName_();
            projectionScreenSettings1.setName(projectionScreenSettings2.getName_());
            projectionScreenSettings2.setName(settings1Name_);
            projectionScreenSettings1.save();
            projectionScreenSettings2.save();
            projectionScreenSettings2.renameSettingsFile2(temporarySwappingScreen, true);
            projectionScreenSettings1.renameSettingsFile2(projectionScreenHolder2.getName(), true);
            projectionScreenSettings2.renameSettingsFile3(temporarySwappingScreen, temp, true);
            projectionScreenHolder1.reload();
            projectionScreenHolder2.reload();
            stage.close();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void initializeVerticalAlignmentSlider(Settings settings) {
        verticalAlignmentSlider.setValue(projectionScreenSettings.getVerticalAlignmentD() * 100);
        setVerticalAlignmentResetVisibility();
        verticalAlignmentSliderReset.setOnAction2(event -> {
            verticalAlignmentSlider.setValue(settings.getVerticalAlignment() * 100);
            projectionScreenSettingsModel.setVerticalAlignment(null);
        });
        verticalAlignmentSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            projectionScreenSettingsModel.setVerticalAlignment(newValue.doubleValue() / 100);
            setVerticalAlignmentResetVisibility();
        });
    }

    private void setVerticalAlignmentResetVisibility() {
        verticalAlignmentSliderReset.setVisible(projectionScreenSettingsModel.getVerticalAlignment() != null);
    }

    private void initializeHorizontalAlignmentSlider(Settings settings) {
        horizontalAlignmentSlider.setValue(projectionScreenSettings.getHorizontalAlignmentD() * 100);
        setHorizontalAlignmentResetVisibility();
        horizontalAlignmentSliderReset.setOnAction2(event -> {
            horizontalAlignmentSlider.setValue(settings.getHorizontalAlignment() * 100);
            projectionScreenSettingsModel.setHorizontalAlignment(null);
        });
        horizontalAlignmentSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            projectionScreenSettingsModel.setHorizontalAlignment(newValue.doubleValue() / 100);
            setHorizontalAlignmentResetVisibility();
        });
    }

    private void setHorizontalAlignmentResetVisibility() {
        horizontalAlignmentSliderReset.setVisible(projectionScreenSettingsModel.getHorizontalAlignment() != null);
    }

    private void initializeProgressBar() {
        progressBarCheckbox.setSelected(projectionScreenSettings.isProgressBar());
        progressBarCheckbox.selectedProperty().addListener((observableValue, oldValue, newValue) ->
                projectionScreenSettingsModel.setProgressBar(newValue));
        initializeHeightSliderOnKeyPressed(progressBarHeightSlider);
        progressBarHeightSlider.setValue(projectionScreenSettings.getProgressBarHeightD() * 100);
        progressBarHeightSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                projectionScreenSettingsModel.setProgressBarHeight(newValue.doubleValue() / 100));
    }

    private void initializeHeightSliderOnKeyPressed(Slider heightSlider) {
        heightSlider.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case UP:
                case RIGHT:
                    changeSliderValue(0.1, heightSlider);
                    break;
                case DOWN:
                case LEFT:
                    changeSliderValue(-0.1, heightSlider);
                    break;
            }
        });
    }

    private void changeSliderValue(double v, Slider heightSlider) {
        heightSlider.setValue(heightSlider.getValue() + v);
    }

    private void initializeNextSection() {
        nextSectionCheckbox.setSelected(projectionScreenSettings.isNextSection());
        nextSectionCheckbox.selectedProperty().addListener((observableValue, oldValue, newValue) ->
                projectionScreenSettingsModel.setNextSection(newValue));
        initializeHeightSliderOnKeyPressed(nextSectionHeightSlider);
        nextSectionHeightSlider.setValue(projectionScreenSettings.getNextSectionHeightD() * 100);
        nextSectionHeightSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                projectionScreenSettingsModel.setNextSectionHeight(newValue.doubleValue() / 100));
    }

    private void initializeTextAlignment(Settings settings) {
        textAlignmentComboBox.getItems().addAll(PTextAlignment.values());
        SingleSelectionModel<PTextAlignment> selectionModel = textAlignmentComboBox.getSelectionModel();
        selectionModel.select(projectionScreenSettings.getTextAlignmentN());
        setTextAlignmentComboBoxResetVisibility();
        textAlignmentComboBoxReset.setOnAction2(event -> {
            selectionModel.select(settings.getTextAlignment());
            projectionScreenSettingsModel.setTextAlignment(null);
        });
        selectionModel.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            projectionScreenSettingsModel.setTextAlignment(newValue);
            setTextAlignmentComboBoxResetVisibility();
        });
    }

    private void setTextAlignmentComboBoxResetVisibility() {
        textAlignmentComboBoxReset.setVisible(projectionScreenSettingsModel.getTextAlignment() != null);
    }

    private void initializeMargins(Settings settings) {
        topMarginTextField.setValue(projectionScreenSettings.getTopMarginD());
        rightMarginTextField.setValue(projectionScreenSettings.getRightMarginD());
        bottomMarginTextField.setValue(projectionScreenSettings.getBottomMarginD());
        leftMarginTextField.setValue(projectionScreenSettings.getLeftMarginD());
        asPaddingCheckbox.setSelected(projectionScreenSettings.isAsPadding());
        setMarginsResetVisibility();
        marginsReset.setOnAction2(event -> {
            topMarginTextField.setValue(settings.getTopMargin());
            rightMarginTextField.setValue(settings.getRightMargin());
            bottomMarginTextField.setValue(settings.getBottomMargin());
            leftMarginTextField.setValue(settings.getLeftMargin());
            asPaddingCheckbox.setSelected(settings.isAsPadding());
            projectionScreenSettingsModel.setTopMargin(null);
            projectionScreenSettingsModel.setRightMargin(null);
            projectionScreenSettingsModel.setBottomMargin(null);
            projectionScreenSettingsModel.setLeftMargin(null);
            projectionScreenSettingsModel.setAsPadding(null);
        });
        marginTextFieldBind(topMarginTextField, projectionScreenSettingsModel::setTopMargin);
        marginTextFieldBind(rightMarginTextField, projectionScreenSettingsModel::setRightMargin);
        marginTextFieldBind(bottomMarginTextField, projectionScreenSettingsModel::setBottomMargin);
        marginTextFieldBind(leftMarginTextField, projectionScreenSettingsModel::setLeftMargin);
        asPaddingCheckbox.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            projectionScreenSettingsModel.setAsPadding(newValue);
            setMarginsResetVisibility();
        });
    }

    private void marginTextFieldBind(TextField marginTextField, DoubleConsumer doubleConsumer) {
        marginTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            doubleConsumer.accept(getDoubleFromTextField(marginTextField));
            setMarginsResetVisibility();
        });
    }

    private void setMarginsResetVisibility() {
        marginsReset.setVisible(projectionScreenSettingsModel.getTopMargin() != null ||
                projectionScreenSettingsModel.getRightMargin() != null ||
                projectionScreenSettingsModel.getBottomMargin() != null ||
                projectionScreenSettingsModel.getLeftMargin() != null ||
                projectionScreenSettingsModel.getAsPadding() != null
        );
    }

    @SuppressWarnings("unused")
    private ChangeListener<Number> getChangeListener() {
        return (observable, oldValue, newValue) -> updatePreview();
    }

    private void updatePreview() {
        ProjectionScreenSettings screenSettings = new ProjectionScreenSettings(projectionScreenSettingsModel);
        screenSettings.setUseGlobalSettings(true);
        // textFlow.setProjectionScreenSettings(screenSettings);
        new Thread(() -> Platform.runLater(() -> {
            // BorderPane mainPane = projectionScreenHolder.getProjectionScreenController().getMainPane();
            // double projectionScreenWidth = Math.max(mainPane.getWidth(), 0.01);
            // double projectionScreenHeight = Math.max(mainPane.getHeight(), 0.01);
            // double width = previewPane.getWidth();
            // double height = projectionScreenHeight * width / projectionScreenWidth;
            // if (height > previewPane.getHeight()) {
            //     height = previewPane.getHeight();
            //     width = projectionScreenWidth * height / projectionScreenHeight;
            // }
            // textFlow.setText2(getPreviewText(), (int) width, (int) height);
            if (liveButton.isSelected()) {
                projectionScreenHolder.onNameChanged();
                ProjectionScreenController projectionScreenController = projectionScreenHolder.getProjectionScreenController();
                if (projectionScreenController == null) {
                    return;
                }
                projectionScreenController.setProjectionScreenSettings(screenSettings);
                projectionScreenController.onSettingsChanged();
            }
        })).start();
    }

    @SuppressWarnings("unused")
    private String getPreviewText() {
        return """
                I love You, Lord
                For Your mercy never failed me
                All my days, I've been held in Your hands
                From the moment that I wake up
                Until I lay my head
                Oh, I will sing of the goodness of God""";
    }

    private void addAndSelectFirstFont(String font) {
        Text tmpK = new Text(font);
        tmpK.setFont(Font.font(font));
        fontListView.getItems().add(tmpK);
        addFontsAndSelectFirstFont(projectionScreenSettings.getFontWeightString());
    }

    private void initializeBackgroundRadio() {
        ToggleGroup group = new ToggleGroup();
        colorRadioButton.setToggleGroup(group);
        imageRadioButton.setToggleGroup(group);
        setBackgroundImageRadioValue(projectionScreenSettings.isBackgroundImage());
    }

    private void setBackgroundImageRadioValue(boolean backgroundImage) {
        imageRadioButton.setSelected(backgroundImage);
        colorRadioButton.setSelected(!backgroundImage);
    }

    private void initializeMaxFontSlider(Settings settings) {
        maxFontSlider.setValue(projectionScreenSettings.getMaxFont());
        setMaxFontSliderResetVisibility();
        maxFontSliderReset.setOnAction2(event -> {
            maxFontSlider.setValue(settings.getMaxFont());
            projectionScreenSettingsModel.setMaxFont(null);
        });
        maxFontSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            projectionScreenSettingsModel.setMaxFont(getIntegerFromNumber(newValue));
            setMaxFontSliderResetVisibility();
        });
    }

    private void initializeBackgroundImagePath(Settings settings) {
        imagePathTextField.setText(projectionScreenSettings.getBackgroundImagePath());
        setImageBrowseButtonResetVisibility();
        imageBrowseButtonReset.setOnAction2(event -> {
            imagePathTextField.setText(settings.getBackgroundImagePath());
            projectionScreenSettingsModel.setBackgroundImagePath(null);
        });
        imagePathTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            projectionScreenSettingsModel.setBackgroundImagePath(newValue);
            setImageBrowseButtonResetVisibility();
        });
    }

    private void setImageBrowseButtonResetVisibility() {
        imageBrowseButtonReset.setVisible(projectionScreenSettingsModel.getBackgroundImagePath() != null);
    }

    private void initializeBreakLinesCheckbox(Settings settings) {
        breakLinesCheckbox.setSelected(projectionScreenSettings.isBreakLines());
        setBreakLinesCheckboxResetVisibility();
        breakLinesCheckboxReset.setOnAction2(event -> {
            breakLinesCheckbox.setSelected(settings.isBreakLines());
            projectionScreenSettingsModel.setBreakLines(null);
        });
        breakLinesCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            projectionScreenSettingsModel.setBreakLines(newValue);
            setBreakLinesCheckboxResetVisibility();
        });
    }

    private void initializeBreakAfterSlider(Settings settings) {
        breakAfterSlider.setValue(projectionScreenSettings.getBreakAfter());
        setBreakAfterSliderResetVisibility();
        breakAfterSliderReset.setOnAction2(event -> {
            breakAfterSlider.setValue(settings.getBreakAfter());
            projectionScreenSettingsModel.setBreakAfter(null);
        });
        breakAfterSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            projectionScreenSettingsModel.setBreakAfter(getIntegerFromNumber(newValue));
            setBreakAfterSliderResetVisibility();
        });
    }

    private void initializeLineSpaceSlider(Settings settings) {
        lineSpaceSlider.setValue(projectionScreenSettings.getLineSpace());
        setLineSpaceSliderResetVisibility();
        lineSpaceSliderReset.setOnAction2(event -> {
            lineSpaceSlider.setValue(settings.getLineSpace());
            projectionScreenSettingsModel.setLineSpace(null);
        });
        lineSpaceSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            projectionScreenSettingsModel.setLineSpace((Double) newValue);
            setLineSpaceSliderResetVisibility();
        });
    }

    private void initializeImageRadioButton() {
        setImageRadioButtonResetVisibility();
        imageRadioButtonReset.setOnAction2(event -> backgroundButtonResetEvent());
        imageRadioButton.selectedProperty().addListener((observable, oldValue, newValue) -> onImageRadioButtonSelected(newValue));
    }

    private void initializeColorRadioButton() {
        setColorRadioButtonResetVisibility();
        colorRadioButtonReset.setOnAction2(event -> backgroundButtonResetEvent());
        // colorRadioButton.selectedProperty().addListener((observable, oldValue, newValue) -> onImageRadioButtonSelected(!newValue));
        // it's already on initializeImageRadioButton
    }

    private void initializeProgressLineColorPicker(Settings settings) {
        progressLineColorPicker.setValue(projectionScreenSettings.getProgressLineColor());
        setProgressLineColorResetVisibility();
        progressLineColorPickerReset.setOnAction2(event -> {
            progressLineColorPicker.setValue(settings.getProgressLineColor());
            projectionScreenSettingsModel.setProgressLineColor(null);
        });
        progressLineColorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            projectionScreenSettingsModel.setProgressLineColor(newValue);
            setProgressLineColorResetVisibility();
        });
    }

    private void initializeProgressLineThicknessSpinner(Settings settings) {
        SpinnerValueFactory.IntegerSpinnerValueFactory spinnerValueFactory = getProgressLineSpinnerValueFactory(projectionScreenSettings.getProgressLineThickness());
        progressLineThicknessSpinner.setValueFactory(spinnerValueFactory);
        setProgressLineThicknessResetVisibility();
        progressLineThicknessSpinnerReset.setOnAction2(event -> {
            spinnerValueFactory.setValue(settings.getProgressLineThickness());
            projectionScreenSettingsModel.setProgressLineThickness(null);
        });
        progressLineThicknessSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            projectionScreenSettingsModel.setProgressLineThickness(newValue);
            setProgressLineThicknessResetVisibility();
        });
    }

    private void initializeProgressLinePosition(Settings settings) {
        ToggleGroup toggleGroup = new ToggleGroup();
        progressLinePositionTopRadioButton.setToggleGroup(toggleGroup);
        progressLinePositionBottomRadioButton.setToggleGroup(toggleGroup);
        setSelectedForProgressLinePosition(projectionScreenSettings.isProgressLinePositionIsTop());
        setProgressLinePositionResetVisibility();
        progressLinePositionReset.setOnAction2(event -> {
            setSelectedForProgressLinePosition(settings.isProgressLinePositionIsTop());
            projectionScreenSettingsModel.setProgressLinePositionIsTop(null);
        });
        progressLinePositionTopRadioButton.selectedProperty().addListener((observable, oldValue, newValue) -> setProgressLinePositionIsTopEvent(newValue));
        progressLinePositionBottomRadioButton.selectedProperty().addListener((observable, oldValue, newValue) -> setProgressLinePositionIsTopEvent(!newValue));
    }

    private void initializeFontWeightComboBox(Settings settings) {
        FontWeight fontWeight = projectionScreenSettings.getFontWeight();
        addFonts(fontWeight, fontListView);
        fontWeightComboBox.getItems().add("NORMAL");
        fontWeightComboBox.getItems().add("BOLD");
        fontWeightComboBox.getSelectionModel().select(projectionScreenSettings.getFontWeightString());
        fontWeightComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            fontWeightValueChange(newValue, projectionScreenSettings.getFont());
            setFontWeightResetVisibility();
        });
        setFontWeightResetVisibility();
        fontWeightComboBoxReset.setOnAction2(event -> {
            fontWeightComboBox.getSelectionModel().select(settings.getFontWeightString());
            projectionScreenSettingsModel.setFontWeight(null);
        });
    }

    private void initializeStrokeFontCheckBox(Settings settings) {
        strokeCheckbox.setSelected(projectionScreenSettings.isStrokeFont());
        setStrokeCheckBoxResetVisibility();
        strokeCheckboxReset.setOnAction2(event -> {
            strokeCheckbox.setSelected(settings.isStrokeFont());
            projectionScreenSettingsModel.setStrokeFont(null);
        });
        strokeCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            projectionScreenSettingsModel.setStrokeFont(newValue);
            setStrokeCheckBoxResetVisibility();
        });
    }

    private void initializeStrokeColorCheckBox(Settings settings) {
        strokeColorPicker.setValue(projectionScreenSettings.getStrokeColor());
        setStrokeColorResetVisibility();
        strokeColorPickerReset.setOnAction2(event -> {
            strokeColorPicker.setValue(settings.getStrokeColor());
            projectionScreenSettingsModel.setStrokeColor(null);
        });
        strokeColorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            projectionScreenSettingsModel.setStrokeColor(newValue);
            setStrokeColorResetVisibility();
        });
    }

    private void initializeStrokeSizeSpinner(Settings settings) {
        SpinnerValueFactory.DoubleSpinnerValueFactory strokeSizeFactory = getStrokeSizeFactory(projectionScreenSettings.getStrokeSizeD());
        strokeSizeSpinner.setValueFactory(strokeSizeFactory);
        setStrokeSizeResetVisibility();
        strokeSizeSpinnerReset.setOnAction2(event -> {
            strokeSizeFactory.setValue(settings.getStrokeSize());
            projectionScreenSettingsModel.setStrokeSize(null);
        });
        strokeSizeSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            projectionScreenSettingsModel.setStrokeSize(newValue);
            setStrokeSizeResetVisibility();
        });
    }

    private void initializeStrokeTypeComboBox(Settings settings) {
        initializeStrokeTypeComboBox_(strokeTypeComboBox, projectionScreenSettings.getStrokeType());
        setStrokeTypeResetVisibility();
        strokeTypeComboBoxReset.setOnAction2(event -> {
            strokeTypeComboBox.getSelectionModel().select(settings.getStrokeType());
            projectionScreenSettingsModel.setStrokeType(null);
        });
        strokeTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            projectionScreenSettingsModel.setStrokeType(newValue);
            setStrokeTypeResetVisibility();
        });
    }

    private void initializeShowSongSecondTextCheckBox(Settings settings) {
        showSongSecondTextCheckBox.setSelected(projectionScreenSettings.isShowSongSecondText());
        setShowSongSecondTextCheckBoxResetVisibility();
        showSongSecondTextCheckBoxReset.setOnAction2(event -> {
            showSongSecondTextCheckBox.setSelected(settings.isShowSongSecondText());
            projectionScreenSettingsModel.setShowSongSecondText(null);
        });
        showSongSecondTextCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            projectionScreenSettingsModel.setShowSongSecondText(newValue);
            setShowSongSecondTextCheckBoxResetVisibility();
        });
    }

    private void initializeSongSecondTextColorPicker(Settings settings) {
        songSecondTextColorPicker.setValue(projectionScreenSettings.getSongSecondTextColor());
        setSongSecondTextColorResetVisibility();
        songSecondTextColorPickerReset.setOnAction2(event -> {
            songSecondTextColorPicker.setValue(settings.getSongSecondTextColor());
            projectionScreenSettingsModel.setSongSecondTextColor(null);
        });
        songSecondTextColorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            projectionScreenSettingsModel.setSongSecondTextColor(newValue);
            setSongSecondTextColorResetVisibility();
        });
    }

    private void initializeFontListView(Settings settings) {
        setFontListViewResetVisibility();
        fontListViewReset.setOnAction2(event -> {
            addAndSelectFirstFont(settings.getFont());
            projectionScreenSettingsModel.setFont(null);
        });
    }

    private void setFontListViewResetVisibility() {
        fontListViewReset.setVisible(projectionScreenSettingsModel.getFont() != null);
    }

    private void setSongSecondTextColorResetVisibility() {
        songSecondTextColorPickerReset.setVisible(projectionScreenSettingsModel.getSongSecondTextColor() != null);
    }

    private void setStrokeCheckBoxResetVisibility() {
        strokeCheckboxReset.setVisible(projectionScreenSettingsModel.getStrokeFont() != null);
    }

    private void setStrokeColorResetVisibility() {
        strokeColorPickerReset.setVisible(projectionScreenSettingsModel.getStrokeColor() != null);
    }

    private void setStrokeSizeResetVisibility() {
        strokeSizeSpinnerReset.setVisible(projectionScreenSettingsModel.getStrokeSize() != null);
    }

    private void setStrokeTypeResetVisibility() {
        strokeTypeComboBoxReset.setVisible(projectionScreenSettingsModel.getStrokeType() != null);
    }

    private void setShowSongSecondTextCheckBoxResetVisibility() {
        showSongSecondTextCheckBoxReset.setVisible(projectionScreenSettingsModel.getShowSongSecondText() != null);
    }

    private void setFontWeightResetVisibility() {
        fontWeightComboBoxReset.setVisible(projectionScreenSettingsModel.getFontWeightString() != null);
    }

    private void setProgressLinePositionIsTopEvent(Boolean newValue) {
        projectionScreenSettingsModel.setProgressLinePositionIsTop(newValue);
        setProgressLinePositionResetVisibility();
    }

    private void setSelectedForProgressLinePosition(boolean progressLinePositionIsTop) {
        progressLinePositionTopRadioButton.setSelected(progressLinePositionIsTop);
        progressLinePositionBottomRadioButton.setSelected(!progressLinePositionIsTop);
    }

    private void setProgressLinePositionResetVisibility() {
        progressLinePositionReset.setVisible(projectionScreenSettingsModel.getProgressLinePosition() != null);
    }

    private void setProgressLineThicknessResetVisibility() {
        progressLineThicknessSpinnerReset.setVisible(projectionScreenSettingsModel.getProgressLineThickness() != null);
    }

    private void setProgressLineColorResetVisibility() {
        progressLineColorPickerReset.setVisible(projectionScreenSettingsModel.getProgressLineColor() != null);
    }

    private void backgroundButtonResetEvent() {
        setBackgroundImageRadioValue(Settings.getInstance().isBackgroundImage());
        projectionScreenSettingsModel.setIsBackgroundImage(null);
        colorRadioButtonReset.setVisible(false);
        imageRadioButtonReset.setVisible(false);
    }

    private void onImageRadioButtonSelected(Boolean newValue) {
        projectionScreenSettingsModel.setIsBackgroundImage(newValue);
        setImageRadioButtonResetVisibility();
        setColorRadioButtonResetVisibility();
    }

    private void setColorRadioButtonResetVisibility() {
        colorRadioButtonReset.setVisible(projectionScreenSettingsModel.getIsBackgroundImage() != null);
    }

    private void setImageRadioButtonResetVisibility() {
        imageRadioButtonReset.setVisible(projectionScreenSettingsModel.getIsBackgroundImage() != null);
    }

    private void setLineSpaceSliderResetVisibility() {
        lineSpaceSliderReset.setVisible(projectionScreenSettingsModel.getLineSpace() != null);
    }

    private void setBreakAfterSliderResetVisibility() {
        breakAfterSliderReset.setVisible(projectionScreenSettingsModel.getBreakAfter() != null);
    }

    private void setBreakLinesCheckboxResetVisibility() {
        breakLinesCheckboxReset.setVisible(projectionScreenSettingsModel.getBreakLines() != null);
    }

    private void initializeBackgroundColorPicker(Settings settings) {
        backgroundColorReset.setOnAction2(event -> {
            backgroundColorPicker.setValue(settings.getBackgroundColor());
            projectionScreenSettingsModel.setBackgroundColor(null);
        });
        backgroundColorPicker.setValue(projectionScreenSettings.getBackgroundColor());
        setBackgroundColorResetVisibility();
        backgroundColorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            projectionScreenSettingsModel.setBackgroundColor(newValue);
            setBackgroundColorResetVisibility();
        });
    }

    private void initializeColorPicker(Settings settings) {
        colorPicker.setValue(projectionScreenSettings.getColor());
        setColorResetVisibility();
        colorReset.setOnAction2(event -> {
            colorPicker.setValue(settings.getColor());
            projectionScreenSettingsModel.setColor(null);
        });
        colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            projectionScreenSettingsModel.setColor(newValue);
            setColorResetVisibility();
        });
    }

    private void setMaxFontSliderResetVisibility() {
        maxFontSliderReset.setVisible(projectionScreenSettingsModel.getMaxFont() != null);
    }

    private void setColorResetVisibility() {
        colorReset.setVisible(projectionScreenSettingsModel.getColor() != null);
    }

    private void setBackgroundColorResetVisibility() {
        backgroundColorReset.setVisible(projectionScreenSettingsModel.getBackgroundColor() != null);
    }

    private void initializeProgressLine() {
        ToggleGroup group = new ToggleGroup();
        progressLinePositionTopRadioButton.setToggleGroup(group);
        progressLinePositionBottomRadioButton.setToggleGroup(group);
        if (projectionScreenSettings.isProgressLinePositionIsTop()) {
            progressLinePositionTopRadioButton.setSelected(true);
        } else {
            progressLinePositionBottomRadioButton.setSelected(true);
        }
    }

    private void changed(ObservableValue<? extends Text> observable, Text oldValue, Text newValue) {
        if (newValue != null && !newValue.getText().isEmpty()) {
            projectionScreenSettingsModel.setFont(newValue.getText());
        }
        setFontListViewResetVisibility();
    }

    private synchronized void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        projectionScreenSettings.setLineSpace(lineSpaceSlider.getValue());
    }

    private void fontWeightValueChange(String fontWeight, String font) {
        projectionScreenSettings.setFontWeight(fontWeight);
        projectionScreenSettingsModel.setFontWeight(fontWeight);
        fontListView.getItems().clear();
        addAndSelectFirstFont(font);
        //        projectionScreenController.reload();
    }

    private void addFontsAndSelectFirstFont(String fontWeight) {
        FontWeight fontWeight1 = getFontWeightByString(fontWeight);
        addFonts(fontWeight1, fontListView);
        fontListView.getSelectionModel().select(0);
    }

    public void onLiveButtonAction() {
        updatePreview();
    }
}
