package projector.controller;

import com.bence.projector.common.dto.ProjectionDTO;
import com.bence.projector.common.dto.SongVerseProjectionDTO;
import com.bence.projector.common.model.SectionType;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.MainDesktop;
import projector.application.ProjectionScreenSettings;
import projector.application.ProjectionType;
import projector.application.ProjectorState;
import projector.application.ScreenProjectionAction;
import projector.application.Settings;
import projector.controller.listener.OnBlankListener;
import projector.controller.listener.ViewChangedListener;
import projector.controller.song.SongController;
import projector.controller.util.AutomaticAction;
import projector.controller.util.ImageCacheService;
import projector.controller.util.OnResultListener;
import projector.controller.util.ProjectionData;
import projector.controller.util.ProjectionScreenHolder;
import projector.controller.util.ProjectionScreensUtil;
import projector.model.CustomCanvas;
import projector.model.Song;
import projector.model.SongVerse;
import projector.utils.SongVerseHolder;
import projector.utils.scene.text.MyTextFlow;
import projector.utils.scene.text.SongVersePartTextFlow;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Math.max;
import static java.lang.Thread.sleep;
import static projector.controller.GalleryController.clearCanvas;
import static projector.controller.MyController.calculateSizeByScale;
import static projector.controller.ProjectionScreensController.getScreenScale;
import static projector.utils.ColorUtil.getColorWithOpacity;
import static projector.utils.ColorUtil.getGeneralTextColorByTheme;
import static projector.utils.CountDownTimerUtil.getDisplayTextFromDateTime;
import static projector.utils.CountDownTimerUtil.getRemainedTime;
import static projector.utils.CountDownTimerUtil.getTimeTextFromDate;
import static projector.utils.SceneUtils.getAStage;
import static projector.utils.SceneUtils.getCustomStage;
import static projector.utils.SceneUtils.getTransparentStage;
import static projector.utils.scene.text.SongVersePartTextFlow.setVisibility;
import static projector.utils.scene.text.TextFlowUtils.getColoredText;
import static projector.utils.scene.text.TextFlowUtils.getItalicText;

public class ProjectionScreenController {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectionScreenController.class);
    private final List<ViewChangedListener> viewChangedListeners = new ArrayList<>();
    private final List<OnBlankListener> onBlankListeners = new ArrayList<>();
    private final List<OnBlankListener> onLockListeners = new ArrayList<>();
    private final String INITIAL_DOT_TEXT = "<color=\"0xffffff0c\">.</color>";
    public BorderPane paneForMargins;
    public BorderPane contentPane;
    public StackPane paneForPadding;
    public StackPane progressBarStackPane;
    public HBox progressBarHBox;
    public BorderPane blackCoverPane;
    public BorderPane progressBarBackgroundBlack;
    private ExecutorService executorService = null;
    @FXML
    private Canvas canvas;
    @FXML
    private MyTextFlow textFlow;
    @FXML
    private MyTextFlow textFlow1;
    @FXML
    private BorderPane mainPane;
    @FXML
    private Pane pane;
    @FXML
    private Pane pane1;
    @FXML
    private Line progressLine;
    private Stage stage;
    private boolean isBlank;
    private Settings settings;
    //    private List<Text> textsList;
    private BibleController bibleController;
    private ProjectionType projectionType = ProjectionType.BIBLE;
    private ProjectionData projectionData;
    private ProjectionScreenController parentProjectionScreenController;
    private ProjectionScreenController doubleProjectionScreenController;
    private ProjectionScreenController previewProjectionScreenController;
    private boolean isLock = false;
    private String activeText = "";
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean broughtToTheFront;
    private Scene scene;
    private List<ProjectionTextChangeListener> projectionImageChangeListeners;
    private boolean countDownTimerRunning = false;
    private Thread countDownTimerThread = null;
    private ProjectionScreenSettings projectionScreenSettings = new ProjectionScreenSettings();
    private Popup popup;
    private Pane root;
    private Screen screen;
    private Stage primaryStage;
    private MainDesktop mainDesktop;
    private boolean setTextCalled = false;
    private GalleryController galleryController;
    private String fileImagePath;
    private Image image = null;
    private double brightness = 0.0;
    private double contrast = 0.0;
    private double saturation = 0.0;
    private Image lastImage = null;
    private int setTextCounter = 0;

    public static BackgroundImage getBackgroundImageByPath(String backgroundImagePath, int width, int height) {
        try {
            Image image = new Image(backgroundImagePath, width, height, false, true);
            return new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        } catch (IllegalArgumentException ignored) {
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    public static Background getBackgroundByPath(String backgroundImagePath, int width, int height) {
        try {
            BackgroundImage backgroundImage = getBackgroundImageByPath(backgroundImagePath, width, height);
            if (backgroundImage == null) {
                return null;
            }
            return new Background(backgroundImage);
        } catch (IllegalArgumentException ignored) {
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    private static void setOpacityAndTitle(Stage stage, double opacity, String key) {
        stage.setOpacity(opacity);
        stage.setTitle(Settings.getInstance().getResourceBundle().getString(key));
    }

    private ExecutorService getExecutorService() {
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }
        return executorService;
    }

    public void initialize() {
        settings = Settings.getInstance();
        progressLine.setVisible(false);
        initializeMargins();
        contentPane.widthProperty().addListener(getContentPaneSizeChangeListener());
        contentPane.heightProperty().addListener(getContentPaneSizeChangeListener());
        setPaneBackground(Color.BLACK, paneForMargins);
        setPaneBackground(Color.BLACK, progressBarBackgroundBlack);
        setProgressBarHeight();
    }

    private ChangeListener<Number> getContentPaneSizeChangeListener() {
        //noinspection CommentedOutCode
        return (observableValue, oldNumber, newNumber) -> {
            // if (false && !changedSignificantly(oldNumber, newNumber)) {
            //     return;
            // }
            repaint();
        };
    }

    @SuppressWarnings("unused")
    private static boolean changedSignificantly(Number oldNumber, Number newNumber) {
        if (oldNumber == null) {
            return newNumber != null;
        }
        double oldValue = oldNumber.doubleValue();
        double newValue = newNumber.doubleValue();
        if (oldValue < newValue) {
            return oldValue / newValue <= 0.99;
        } else {
            return newValue / oldValue <= 0.99;
        }
    }

    private void setProgressBarHeight() {
        double height = paneForPadding.getHeight() * projectionScreenSettings.getProgressBarHeightD();
        List<HBox> hBoxes = getProgressBarHBoxes();
        for (HBox hBox : hBoxes) {
            setHBoxSizes(hBox, paneForPadding.getWidth(), height);
        }
    }

    private void setHBoxSizes(HBox hBox, double width, double height) {
        hBox.setPrefWidth(width);
        hBox.setMaxWidth(width);
        hBox.setPrefHeight(height);
        hBox.setMaxHeight(height);
    }

    private ArrayList<HBox> getProgressBarHBoxes() {
        ArrayList<HBox> hBoxes = new ArrayList<>();
        // hBoxes.add(progressBarBackgroundHBox);
        hBoxes.add(progressBarHBox);
        // hBoxes.add(progressBarTopHBox);
        return hBoxes;
    }

    public void setOnMainProjectionEvent() {
        contentPane.setOnMousePressed(e -> {
            if (isLock) {// ) || !stage.isMaximized()) {
                return;
            }
            int width = (int) contentPane.getWidth();
            boolean next = (double) width / 2 < e.getX();
            if (projectionType == ProjectionType.BIBLE) {
                if (bibleController != null) {
                    if (next) {
                        bibleController.setNextVerse();
                        // stage.setOpacity(0.5);
                    } else {
                        bibleController.setPreviousVerse();
                        // stage.setOpacity(1);
                    }
                }
            } else if (projectionType == ProjectionType.SONG) {
                SongController songController = getSongController();
                if (songController != null) {
                    if (next) {
                        songController.setNext();
                    } else {
                        songController.setPrevious();
                    }
                }
            } else if (projectionType == ProjectionType.IMAGE) {
                if (galleryController != null) {
                    if (next) {
                        galleryController.setNext();
                    } else {
                        galleryController.setPrevious();
                    }
                }
            }
        });
    }

    private SongController getSongController() {
        return MyController.getInstance().getSongController();
    }

    private void initializeFromSettings() {
        progressLine.setStroke(projectionScreenSettings.getProgressLineColor());
        settings.showProgressLineProperty().addListener((observable, oldValue, newValue) -> {
            boolean isSong = projectionType == ProjectionType.SONG;
            progressLine.setVisible(newValue && isSong);
            setVisibility(progressBarHBox, newValue && isSong);
        });
        settings.progressLinePositionIsTopProperty().addListener((observable, oldValue, newValue) -> setProgressLineProperties());
        this.textFlow.setProjectionScreenSettings(projectionScreenSettings);
        this.textFlow1.setProjectionScreenSettings(projectionScreenSettings);
    }

    public void setBackGroundColor() {
        setBackGroundColor2();
    }

    public void setBackGroundColor2() {
        if (isLock) {
            return;
        }
        Platform.runLater(this::setBackgroundBySettings);
    }

    private void setBackgroundBySettings() {
        if (!projectionScreenSettings.isBackgroundImage()) {
            Color backgroundColor = projectionScreenSettings.getBackgroundColor();
            setPanesBackground(backgroundColor);
        } else {
            setBackGroundImage();
        }
        onViewChanged();
    }

    private void setPanesBackground(Color backgroundColor) {
        setPaneBackground(backgroundColor, contentPane);
        setPaneBackground(backgroundColor, paneForMargins);
    }

    private void setPaneBackground(Color backgroundColor, Pane pane) {
        BackgroundFill myBF = new BackgroundFill(backgroundColor, new CornerRadii(1), new Insets(0.0, 0.0, 0.0, 0.0));
        pane.setBackground(new Background(myBF));
    }

    private void setBackGroundImage() {
        if (isLock) {
            return;
        }
        if (projectionScreenSettings.isBackgroundImage()) {
            int w = 80;
            int h = 60;
            if (contentPane != null) {
                w = (int) contentPane.getWidth();
                h = (int) contentPane.getHeight();
            }
            Background background = getBackgroundByPath(projectionScreenSettings.getBackgroundImagePath(), w, h);
            if (background != null) {
                contentPane.setBackground(background);
            }
        }
    }

    public void setBlank(boolean isBlank) {
        setBlankLocally(isBlank);
    }

    private void setBlankLocally(boolean isBlank) {
        this.isBlank = isBlank;
        pane.setVisible(!isBlank);
        pane1.setVisible(!isBlank);
        blackCoverPane.setVisible(isBlank);
        setPaneBackground(Color.BLACK, blackCoverPane);
        onViewChanged();
        onBlankChanged();
    }

    private void onBlankChanged() {
        for (OnBlankListener onBlankListener : onBlankListeners) {
            onBlankListener.onBlankChanged(isBlank);
        }
    }

    public void repaint() {
        if (isLock) {
            return;
        }
        initializeMargins();
        if (projectionType == ProjectionType.IMAGE) {
            if (fileImagePath != null) {
                setImage(fileImagePath, projectionType, null);
            } else {
                drawImage(this.lastImage);
            }
            return;
        }
        setText5(activeText, projectionType, projectionData);
        setBackGroundColor2();
        setProgressLineProperties();
        setProgressBarHeight();
    }

    public void setCountDownTimer(Date finishedDate, AutomaticAction automaticAction, boolean showFinishTime) {
        countDownTimerRunning = false;
        if (countDownTimerThread != null) {
            try {
                countDownTimerThread.join();
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
        countDownTimerThread = new Thread(() -> {
            String previousTimeText = "";
            while (countDownTimerRunning && settings.isApplicationRunning()) {
                try {
                    Long remainedTime = getRemainedTime(finishedDate);
                    if (remainedTime != null && remainedTime <= 0 && automaticAction != AutomaticAction.COUNTDOWN_TIMER_ENDLESS) {
                        countDownTimerRunning = false;
                        doTheAutomaticAction(automaticAction);
                        break;
                    }
                    String timeTextFromDate = getTimeTextFromDate(remainedTime, automaticAction);
                    int millis = 100;
                    if (timeTextFromDate.equals(previousTimeText)) {
                        //noinspection BusyWait
                        sleep(millis);
                        continue;
                    }
                    if (!timeTextFromDate.isEmpty() && !activeText.equals(timeTextFromDate)) {
                        previousTimeText = timeTextFromDate;
                        Platform.runLater(() -> {
                            String s = timeTextFromDate;
                            s = addFinishTime(finishedDate, showFinishTime, s);
                            setText(s, ProjectionType.COUNTDOWN_TIMER_PROCESS, projectionData);
                        });
                    }
                    //noinspection BusyWait
                    sleep(millis);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        countDownTimerRunning = true;
        countDownTimerThread.start();
    }

    private String addFinishTime(Date finishedDate, boolean showFinishTime, String s) {
        if (showFinishTime) {
            String finishedText = getDisplayTextFromDateTime(finishedDate);
            finishedText = getColoredText(finishedText, getColorForFinishedText());
            finishedText = getItalicText((finishedText));
            s += "\n" + finishedText;
        }
        return s;
    }

    private Color getColorForFinishedText() {
        try {
            Color color = projectionScreenSettings.getColor();
            return color.darker();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Color.rgb(255, 255, 255);
        }
    }

    private void doTheAutomaticAction(AutomaticAction automaticAction) {
        if (automaticAction == AutomaticAction.NOTHING) {
            return;
        }
        Platform.runLater(() -> {
            switch (automaticAction) {
                case EMPTY -> setText("", ProjectionType.SONG, projectionData);
                case SONG_TITLE -> getSongController().selectSongTitle();
            }
        });
    }

    public void setText2(String newText, ProjectionType projectionType) {
        setText(newText, projectionType, null);
    }

    public void clearText() {
        setText2("", ProjectionType.CLEAR); // this should send also over network. The simple clear button not
    }

    public void setText(String newText, ProjectionType projectionType, ProjectionData projectionData) {
        setText5(newText, projectionType, projectionData);
    }

    private void setText5(String newText, ProjectionType projectionType, ProjectionData projectionData) {
        if (projectionType == ProjectionType.SONG) {
            setSongVerseProjection(projectionData, newText);
        } else {
            setText3(newText, projectionType, projectionData);
        }
    }

    private void setSongVerseProjection(ProjectionData projectionData, String text) {
        if (projectionData != null) {
            List<SongVerseProjectionDTO> songVerseProjectionDTOS = null;
            ProjectionDTO projectionDTO = projectionData.getProjectionDTO();
            if (projectionDTO != null) {
                songVerseProjectionDTOS = projectionDTO.getSongVerseProjectionDTOS();
            }
            if (songVerseProjectionDTOS != null) {
                if (projectionScreenSettings.isFocusOnSongPart()) {
                    String focusedText = getFocusedText(songVerseProjectionDTOS);
                    if (!focusedText.isEmpty()) {
                        setText3(focusedText, ProjectionType.SONG, projectionData);
                        return;
                    }
                }
                String wholeWithFocusedText = getWholeWithFocusedText(songVerseProjectionDTOS);
                if (!wholeWithFocusedText.trim().isEmpty()) {
                    text = wholeWithFocusedText;
                }
            }
        }
        setText3(text, ProjectionType.SONG, projectionData);
    }

    private static String getWholeWithFocusedText(List<SongVerseProjectionDTO> songVerseProjectionDTOS) {
        HashMap<Integer, Boolean> songVerseIndexMap = new HashMap<>();
        StringBuilder wholeWithFocusedText = new StringBuilder();
        boolean first = true;
        HashMap<Integer, HashMap<Integer, Boolean>> focusedIndicesMap = new HashMap<>();
        for (SongVerseProjectionDTO songVerseProjectionDTO : songVerseProjectionDTOS) {
            Integer songVerseIndex = songVerseProjectionDTO.getSongVerseIndex();
            HashMap<Integer, Boolean> focusedIndices = focusedIndicesMap.computeIfAbsent(songVerseIndex, k -> new HashMap<>());
            focusedIndices.put(songVerseProjectionDTO.getFocusedTextIndex(), true);
        }
        for (SongVerseProjectionDTO songVerseProjectionDTO : songVerseProjectionDTOS) {
            Integer songVerseIndex = songVerseProjectionDTO.getSongVerseIndex();
            if (songVerseIndexMap.containsKey(songVerseIndex)) {
                continue;
            }
            songVerseIndexMap.put(songVerseIndex, true);
            if (!first) {
                wholeWithFocusedText.append("\n");
            } else {
                first = false;
            }
            HashMap<Integer, Boolean> focusedIndices = focusedIndicesMap.get(songVerseIndex);
            wholeWithFocusedText.append(SongController.getWholeWithFocusedText(songVerseProjectionDTO.getTexts(), focusedIndices));
        }
        return wholeWithFocusedText.toString();
    }

    private static String getFocusedText(List<SongVerseProjectionDTO> songVerseProjectionDTOS) {
        StringBuilder focusedText = new StringBuilder();
        boolean first = true;
        for (SongVerseProjectionDTO songVerseProjectionDTO : songVerseProjectionDTOS) {
            if (!first) {
                focusedText.append("\n");
            } else {
                first = false;
            }
            focusedText.append(songVerseProjectionDTO.getFocusedText());
        }
        return focusedText.toString();
    }

    private void setText3(String newText, ProjectionType projectionType, ProjectionData projectionData) {
        if (!newText.equals(INITIAL_DOT_TEXT)) {
            this.setTextCalled = true;
        }
        if (!projectionType.isCountdownTimer() && !isLock) {
            countDownTimerRunning = false;
        }
        this.setTextCounter++;
        Platform.runLater(() -> {
            --this.setTextCounter;
            if (this.setTextCounter > 0) {
                return;
            }
            hideImageIfNotImageType(projectionType);
            this.projectionType = projectionType;
            activeText = newText;
            this.projectionData = projectionData;
            if (isLock && projectionType != ProjectionType.COUNTDOWN_TIMER_PROCESS) {
                return;
            }
            handleSongProgressBar(projectionType);
            if (handleByProjectionScreenSettingsAction()) {
                return;
            }
            int width = (int) (contentPane.getWidth());
            int height = (int) contentPane.getHeight();
            if (projectionType == ProjectionType.REFERENCE) {
                textFlow1.setText2(newText, width, height);
                double v = projectionScreenSettings.getMaxFont() * 0.7;
                if (textFlow1.getSize() < v && newText.length() > 100) {
                    String[] split = splitHalfByNewLine(newText);
                    textFlow.setText2(split[0], width / 2, height);
                    textFlow1.setText2(split[1], width / 2, height);
                    int textFlowSize = textFlow.getSize();
                    int textFlow1Size = textFlow1.getSize();
                    if (textFlowSize > textFlow1Size) {
                        textFlow.setSizeAndAlign(textFlow1Size);
                    } else if (textFlowSize < textFlow1Size) {
                        textFlow1.setSizeAndAlign(textFlowSize);
                    }
                    return;
                }
            }
            if (projectionType == ProjectionType.SONG) {
                progressLine.setVisible(settings.isShowProgressLine());
            } else {
                progressLine.setVisible(false);
            }
            textFlow.setText2(newText, width, height);
            textFlow.setTextAlignment(projectionScreenSettings.getTextAlignmentT());
            alignX();
            textFlow1.setText2("", 0, height);
            onViewChanged();
        });
    }

    private Song getSong() {
        if (projectionData == null) {
            return null;
        }
        return projectionData.getSong();
    }

    private void handleSongProgressBar(ProjectionType projectionType) {
        List<HBox> hBoxes = getProgressBarHBoxes();
        for (HBox hBox : hBoxes) {
            hBox.getChildren().clear();
        }
        Song song = getSong();
        boolean visible = projectionType == ProjectionType.SONG && song != null && projectionScreenSettings.isProgressBar() && settings.isShowProgressLine();
        if (visible) {
            List<SongVersePartTextFlow> songVersePartTextFlows = projectionData.getSongVersePartTextFlows();
            int n = songVersePartTextFlows.size();
            progressBarHBox.setPrefWidth(paneForPadding.getWidth());
            double width = paneForPadding.getWidth() / n;
            double height = progressBarHBox.getPrefHeight();
            List<SongVerse> songVerses = song.getSongVersesByVerseOrder();
            if (songVerses == null) {
                return;
            }
            createProgressBarPanes(song, width, height);
        }
        if (projectionType == ProjectionType.CLEAR) {
            progressBarHBox.setVisible(false);
        } else {
            setVisibility(progressBarHBox, visible);
        }
    }

    private void createProgressBarPanes(Song song, double width, double height) {
        ObservableList<Node> progressBarHBoxChildren = progressBarHBox.getChildren();
        int i = 0;
        List<SongVerseHolder> songVerseHolders = song.getSongVerseHolders();
        for (SongVerseHolder songVerseHolder : songVerseHolders) {
            SongVerse songVerse = songVerseHolder.getSongVerse();
            double currentSongVerseWidth = width * songVerse.getSplitTexts().size();
            StackPane stackPane = new StackPane();
            stackPane.setPrefWidth(currentSongVerseWidth);
            stackPane.setPrefHeight(height);
            stackPane.setMaxWidth(currentSongVerseWidth);
            stackPane.setMaxHeight(height);
            Pane pane = new Pane();
            pane.setPrefWidth(currentSongVerseWidth);
            pane.setMaxWidth(currentSongVerseWidth);
            pane.setPrefHeight(height);
            pane.setMaxHeight(height);
            pane.getChildren().add(stackPane);
            pane.setClip(new Rectangle(currentSongVerseWidth, height));
            progressBarHBoxChildren.add(pane);
            List<SongVersePartTextFlow> songVersePartTextFlows = songVerseHolder.getSongVersePartTextFlows();
            ObservableList<Node> stackPaneChildren = stackPane.getChildren();

            createProgressBarBackgroundPane(songVersePartTextFlows, currentSongVerseWidth, width, stackPaneChildren);
            addSectionText(height, songVerse, currentSongVerseWidth, stackPaneChildren);

            SongVersePartTextFlow previousLast = getLastSongVersePartTextFlow(getFromList(songVerseHolders, i - 1));
            SongVersePartTextFlow nextFirst = getFirstSongVersePartTextFlow(getFromList(songVerseHolders, i + 1));
            createProgressBarTopPane(songVersePartTextFlows, currentSongVerseWidth, width, stackPaneChildren, previousLast, nextFirst);
            ++i;
        }
    }

    private SongVersePartTextFlow getFirstSongVersePartTextFlow(SongVerseHolder songVerseHolder) {
        if (songVerseHolder == null) {
            return null;
        }
        List<SongVersePartTextFlow> songVersePartTextFlows = songVerseHolder.getSongVersePartTextFlows();
        return getFromList(songVersePartTextFlows, 0);
    }

    private SongVersePartTextFlow getLastSongVersePartTextFlow(SongVerseHolder songVerseHolder) {
        if (songVerseHolder == null) {
            return null;
        }
        List<SongVersePartTextFlow> songVersePartTextFlows = songVerseHolder.getSongVersePartTextFlows();
        return getFromList(songVersePartTextFlows, songVersePartTextFlows.size() - 1);
    }

    private void createProgressBarBackgroundPane(List<SongVersePartTextFlow> songVersePartTextFlows, double currentSongVerseWidth, double width, ObservableList<Node> stackPaneChildren) {
        HBox hBox = new HBox();
        setHBoxSizes(hBox, currentSongVerseWidth, progressBarHBox.getPrefHeight());
        hBox.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(0), new BorderWidths(1))));
        ObservableList<Node> hBoxChildren = hBox.getChildren();
        for (SongVersePartTextFlow songVersePartTextFlow : songVersePartTextFlows) {
            Pane pane = new Pane();
            pane.setPrefWidth(width);
            hBoxChildren.add(pane);
            SectionType sectionType = songVersePartTextFlow.getSongVerse().getSectionType();
            Color backgroundColor = Color.web(sectionType.getBackgroundColorHex(true));
            songVersePartTextFlow.opacityProperty().addListener((observableValue, oldValue, newValue) -> setPaneBackgroundWithOpacity(songVersePartTextFlow, backgroundColor, pane));
            setPaneBackgroundWithOpacity(songVersePartTextFlow, backgroundColor, pane);
        }
        stackPaneChildren.add(hBox);
    }

    private static void addSectionText(double height, SongVerse songVerse, double currentSongVerseWidth, ObservableList<Node> stackPaneChildren) {
        Text text = new Text(songVerse.getSectionTypeStringWithCount());
        int v = 80;
        text.setFont(Font.font(v));
        text.setFill(getGeneralTextColorByTheme(true));
        double scaleFactor = height / text.getBoundsInLocal().getHeight();
        if (scaleFactor < 1) {
            text.setFont(Font.font(v * scaleFactor));
        }
        scaleFactor = currentSongVerseWidth / text.getBoundsInLocal().getWidth();
        if (scaleFactor < 1) //noinspection CommentedOutCode
        {
            text.setFont(Font.font(text.getFont().getSize() * scaleFactor));
            // leaves a black space
            // text.setTranslateX(-(text.getBoundsInLocal().getWidth() - currentSongVerseWidth) / 2);
            // text.setScaleX(scaleFactor);
        }
        stackPaneChildren.add(text);
    }

    private void createProgressBarTopPane(List<SongVersePartTextFlow> songVersePartTextFlows, double currentSongVerseWidth, double width, ObservableList<Node> stackPaneChildren, SongVersePartTextFlow previousLast, SongVersePartTextFlow nextFirst) {
        double progressBarSelectionBorderSize = 24.0 * projectionScreenSettings.getProgressBarHeightD();
        HBox hBox = new HBox();
        setHBoxSizes(hBox, currentSongVerseWidth, progressBarHBox.getPrefHeight());
        ObservableList<Node> hBoxChildren = hBox.getChildren();
        int i = -1;
        for (SongVersePartTextFlow songVersePartTextFlow : songVersePartTextFlows) {
            ++i;
            Pane pane = new Pane();
            pane.setPrefWidth(width);
            hBoxChildren.add(pane);
            if (songVersePartTextFlow.isSelected()) {
                Glow glow = new Glow();
                glow.setLevel(0.5);
                pane.setEffect(glow);
                SongVersePartTextFlow previousSongVersePartTextFlow = getFromListWithDefault(songVersePartTextFlows, i - 1, previousLast);
                SongVersePartTextFlow nextSongVersePartTextFlow = getFromListWithDefault(songVersePartTextFlows, i + 1, nextFirst);
                double leftBorderWidth = isSongVersePartTextFlowSelected(previousSongVersePartTextFlow) ? 0 : progressBarSelectionBorderSize;
                double rightBorderWidth = isSongVersePartTextFlowSelected(nextSongVersePartTextFlow) ? 0 : progressBarSelectionBorderSize;
                pane.setBorder(new Border(new BorderStroke(settings.getProgressLineColor(), BorderStrokeStyle.SOLID, new CornerRadii(0), new BorderWidths(progressBarSelectionBorderSize, rightBorderWidth, progressBarSelectionBorderSize, leftBorderWidth))));
            }
        }
        stackPaneChildren.add(hBox);
    }

    private boolean isSongVersePartTextFlowSelected(SongVersePartTextFlow songVersePartTextFlow) {
        if (songVersePartTextFlow == null) {
            return false;
        }
        return songVersePartTextFlow.isSelected();
    }

    private <T> T getFromList(List<T> songVersePartTextFlows, int i) {
        if (songVersePartTextFlows.size() <= i || i < 0) {
            return null;
        }
        return songVersePartTextFlows.get(i);
    }

    private SongVersePartTextFlow getFromListWithDefault(List<SongVersePartTextFlow> songVersePartTextFlows, int i, SongVersePartTextFlow defaultSongVersePart) {
        SongVersePartTextFlow songVersePartTextFlow = getFromList(songVersePartTextFlows, i);
        if (songVersePartTextFlow == null) {
            return defaultSongVersePart;
        }
        return songVersePartTextFlow;
    }

    private void setPaneBackgroundWithOpacity(SongVersePartTextFlow songVersePartTextFlow, Color backgroundColor, Pane pane) {
        if (isLock()) {
            return;
        }
        Color backgroundColorWithOpacity = getColorWithOpacity(backgroundColor, songVersePartTextFlow.getOpacity());
        setPaneBackground(backgroundColorWithOpacity, pane);
    }

    private static double getHorizontalCorrigateByTextAlignment(TextAlignment textAlignment) {
        if (textAlignment.equals(TextAlignment.CENTER)) {
            return -0.5;
        }
        if (textAlignment.equals(TextAlignment.RIGHT)) {
            return -1.0;
        }
        return 0.0;
    }

    private void alignX() {
        double horizontalAlignment = projectionScreenSettings.getHorizontalAlignmentD();
        if (shouldBeDefaultAlign(horizontalAlignment)) {
            return;
        }
        double shift = textFlow.getWidth() - textFlow.getMaxLineWidth();
        double corrigateByTextAlignment = getHorizontalCorrigateByTextAlignment(textFlow.getTextAlignment());
        double x = shift * (horizontalAlignment + corrigateByTextAlignment);
        textFlow.setLayoutX(x);
    }

    private boolean shouldBeDefaultAlign(double alignment) {
        if (!textFlow.getTextAlignment().equals(TextAlignment.CENTER)) {
            return false;
        }
        return Math.abs(alignment - 0.5) < 0.01;
    }

    private boolean handleByProjectionScreenSettingsAction() {
        ScreenProjectionAction screenProjectionAction = projectionScreenSettings.getScreenProjectionAction(projectionType);
        switch (screenProjectionAction) {
            case DISPLAY -> {
                return false;
            }
            case NO_ACTION -> {
                return true;
            }
            case CLEAR -> {
                clear();
                return true;
            }
        }
        return false;
    }

    private void clear() {
        textFlow.setText2("", 0, 10);
        textFlow1.setText2("", 0, 10);
        progressLine.setVisible(false);
        progressBarHBox.setVisible(false);
        hideCanvas();
        onViewChanged();
        stopCountDownTimer();
    }

    public void clearAll() {
        if (isLock) {
            return;
        }
        clear();
    }

    private void hideImageIfNotImageType(ProjectionType projectionType) {
        if (projectionType != ProjectionType.IMAGE) {
            hideCanvas();
        }
    }

    private void hideCanvas() {
        canvas.setVisible(false);
    }

    private String[] splitHalfByNewLine(String newText) {
        String[] split = newText.split("\n");
        int i;
        int half = split.length / 2;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(split[0]);
        for (i = 1; i < split.length; ++i) {
            if (i >= half) {
                break;
            }
            stringBuilder.append("\n").append(split[i]);
        }
        String[] returnValue = new String[2];
        returnValue[0] = stringBuilder.toString();
        stringBuilder = new StringBuilder();
        stringBuilder.append(split[i++]);
        for (; i < split.length; ++i) {
            stringBuilder.append("\n").append(split[i]);
        }
        returnValue[1] = stringBuilder.toString();
        return returnValue;
    }

    void setBibleController(BibleController bibleController) {
        this.bibleController = bibleController;
    }

    private void setStyleFile(Scene scene) {
        URL resource = getClass().getResource("/view/" + settings.getSceneStyleFile());
        if (resource != null) {
            scene.getStylesheets().add(resource.toExternalForm());
        }
    }

    void duplicate() {
        if (doubleProjectionScreenController == null) {
            FXMLLoader loader2 = new FXMLLoader();
            loader2.setLocation(MainDesktop.class.getResource("/view/ProjectionScreen.fxml"));
            Pane root2;
            try {
                root2 = loader2.load();

                doubleProjectionScreenController = loader2.getController();
                ProjectionScreensUtil.getInstance().addDoubleProjectionScreenController(doubleProjectionScreenController);
                Scene scene2 = new Scene(root2, 400, 300);
                setStyleFile(scene2);

                scene2.widthProperty().addListener((observable, oldValue, newValue) -> doubleProjectionScreenController.repaint());
                scene2.heightProperty().addListener((observable, oldValue, newValue) -> doubleProjectionScreenController.repaint());
                Stage stage2 = getAStage(getClass());
                stage2.setScene(scene2);
                stage2.setTitle(doubleProjectionScreenController.getProjectionScreenSettings().getProjectionScreenHolder().getProjectionScreenSettings().getName());
                stage2.setX(0);
                stage2.setY(0);
                doubleProjectionScreenController.setStage(stage2);
                scene2.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.F11) {
                        stage2.setMaximized(!stage2.isMaximized());
                    } else if (event.getCode().equals(KeyCode.ESCAPE)) {
                        stage2.setMaximized(false);
                    }
                });
                stage2.initStyle(StageStyle.UNDECORATED);
                stage2.show();
                stage2.maximizedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        scene2.setCursor(Cursor.NONE);
                    } else {
                        scene2.setCursor(Cursor.DEFAULT);
                    }
                });
                scene2.setOnMousePressed(event -> {
                    xOffset = event.getSceneX();
                    yOffset = event.getSceneY();
                });
                scene2.setOnMouseDragged(event -> {
                    if (!stage2.isMaximized()) {
                        stage2.setX(event.getScreenX() - xOffset);
                        stage2.setY(event.getScreenY() - yOffset);
                    }
                });

                stage2.fullScreenProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue) {
                        stage2.setMaximized(false);
                    }
                });
                duplicateCanvasClose(stage2);
                setSomeInitializationForDoubleProjectionScreenController(doubleProjectionScreenController);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        } else {
            doubleProjectionScreenController.duplicate();
        }
    }

    private void duplicateCanvasClose(Stage stage2) {
        doubleProjectionScreenController.setParentProjectionScreenController(doubleProjectionScreenController);
        stage2.setOnCloseRequest(we -> {
            stage2.close();
            linkedMerge(doubleProjectionScreenController, parentProjectionScreenController);
        });
    }

    private void linkedMerge(ProjectionScreenController doubleProjectionScreenController, ProjectionScreenController parentProjectionScreenController) {
        if (doubleProjectionScreenController != null) {
            ProjectionScreensUtil.getInstance().removeProjectionScreenController(doubleProjectionScreenController);
            doubleProjectionScreenController.setParentProjectionScreenController(parentProjectionScreenController);
            if (parentProjectionScreenController != null) {
                parentProjectionScreenController.setDoubleProjectionScreenController(doubleProjectionScreenController);
            }
        }
    }

    private void removeProjectionScreenController(ProjectionScreenController doubleProjectionScreenController) {
        if (doubleProjectionScreenController != null) {
            ProjectionScreensUtil.getInstance().removeProjectionScreenController(doubleProjectionScreenController);
        }
    }

    public ProjectionScreenController duplicate2() {
        FXMLLoader loader2 = new FXMLLoader();
        loader2.setLocation(MainDesktop.class.getResource("/view/ProjectionScreen.fxml"));
        try {
            Pane root = loader2.load();
            ProjectionScreenController doubleProjectionScreenController = loader2.getController();
            doubleProjectionScreenController.setRoot(root);
            ProjectionScreensUtil.getInstance().addAutomaticDoubleProjectionScreenController(doubleProjectionScreenController);
            setSomeInitializationForDoubleProjectionScreenController(doubleProjectionScreenController);
            return doubleProjectionScreenController;
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    private void setSomeInitializationForDoubleProjectionScreenController(ProjectionScreenController doubleProjectionScreenController) {
        doubleProjectionScreenController.setBlank(isBlank);
        doubleProjectionScreenController.setParentProjectionScreenController(doubleProjectionScreenController);
        doubleProjectionScreenController.setText(activeText, projectionType, projectionData);
    }

    public void createCustomStageWithIterator(Iterator<CustomCanvas> iterator) {
        if (iterator == null || !iterator.hasNext()) {
            return;
        }
        createCustomStage2(iterator.next(), iterator);
    }

    public void createNewCustomStage(CustomCanvas customCanvas) {
        createCustomStage2(customCanvas, null);
    }

    public void createCustomStage2(CustomCanvas customCanvas, Iterator<CustomCanvas> iterator) {
        if (customCanvas == null) {
            return;
        }
        double width = calculateSizeByScale(customCanvas.getWidth());
        double height = calculateSizeByScale(customCanvas.getHeight());
        FXMLLoader loader2 = new FXMLLoader();
        loader2.setLocation(MainDesktop.class.getResource("/view/ProjectionScreen.fxml"));
        Pane root2;
        try {
            root2 = loader2.load();

            ProjectionScreenController customStageController = loader2.getController();
            Scene scene2 = new Scene(root2, width, height);
            setStyleFile(scene2);

            scene2.widthProperty().addListener((observable, oldValue, newValue) -> customStageController.repaint());
            scene2.heightProperty().addListener((observable, oldValue, newValue) -> customStageController.repaint());
            Stage stage2 = getTransparentStage(getClass());
            stage2.setTitle(customCanvas.getName());
            ProjectionScreenHolder projectionScreenHolder = ProjectionScreensUtil.getInstance().addProjectionScreenController(customStageController, stage2.getTitle());
            customCanvas.setProjectionScreenHolder(projectionScreenHolder);
            projectionScreenHolder.setStage(stage2);
            projectionScreenHolder.setScreenIndex(0);
            customStageController.setScreen(Screen.getPrimary());
            scene2.setFill(Color.TRANSPARENT);
            stage2.setScene(scene2);

            stage2.setX(calculateSizeByScale(customCanvas.getPositionX()));
            stage2.setY(calculateSizeByScale(customCanvas.getPositionY()));
            customStageController.setStage(stage2);
            scene2.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.F11) {
                    stage2.setMaximized(!stage2.isMaximized());
                } else if (event.getCode().equals(KeyCode.ESCAPE)) {
                    stage2.setMaximized(false);
                }
            });
            stage2.show();
            stage2.maximizedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    scene2.setCursor(Cursor.NONE);
                } else {
                    scene2.setCursor(Cursor.DEFAULT);
                }
            });
            scene2.setOnMousePressed(event -> {
                customStageController.xOffset = event.getSceneX();
                customStageController.yOffset = event.getSceneY();
            });
            scene2.setOnMouseDragged(event -> {
                if (!stage2.isMaximized()) {
                    stage2.setX(event.getScreenX() - customStageController.xOffset);
                    stage2.setY(event.getScreenY() - customStageController.yOffset);
                }
            });

            stage2.fullScreenProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    stage2.setMaximized(false);
                }
            });
            customCanvasClose(stage2, customCanvas, () -> onCloseCustomCanvasStage(customStageController));
            customStageController.setBlank(isBlank);
            customStageController.setText(activeText, projectionType, projectionData);
            customCanvas.setStage(stage2);
            customStageController.createCustomStageWithIterator(iterator);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private static void customCanvasClose(Stage stage2, CustomCanvas customCanvas, OnResultListener onCloseCustomCanvasStage) {
        customCanvas.setCloseListener(onCloseCustomCanvasStage);
        stage2.setOnCloseRequest(we -> {
            stage2.close();
            onCloseCustomCanvasStage.onResult();
        });
    }

    private void onCloseCustomCanvasStage(ProjectionScreenController customStageController) {
        removeProjectionScreenController(customStageController);
    }

    public void createPreview() {
        if (previewProjectionScreenController == null) {
            FXMLLoader loader2 = new FXMLLoader();
            loader2.setLocation(MainDesktop.class.getResource("/view/ProjectionScreen.fxml"));
            Pane root2;
            try {
                root2 = loader2.load();

                previewProjectionScreenController = loader2.getController();
                String title = Settings.getInstance().getResourceBundle().getString("Preview");
                ProjectionScreenHolder projectionScreenHolder = ProjectionScreensUtil.getInstance().addProjectionScreenController(previewProjectionScreenController, title);
                projectionScreenHolder.setPreview(true);
                previewProjectionScreenController.setScreen(Screen.getPrimary());
                projectionScreenHolder.setScreenIndex(0);
                double ratio = getSceneAspectRatio(contentPane.getScene());
                double size = 512;
                if (settings.getPreviewWidth() > 0) {
                    size = settings.getPreviewWidth();
                }
                double width = size;
                double height = size * ratio;
                Scene scene2 = new Scene(root2, width, height);
                setStyleFile(scene2);
                Stage stage2 = getCustomStage(getClass(), scene2);
                Scene previewWindowScene = stage2.getScene();
                previewWindowScene.widthProperty().addListener((observable, oldValue, newValue) -> previewProjectionScreenController.repaint());
                previewWindowScene.heightProperty().addListener((observable, oldValue, newValue) -> previewProjectionScreenController.repaint());
                stage2.setWidth(width);
                stage2.setHeight(height);

                stage2.setX(settings.getPreviewX());
                stage2.setY(settings.getPreviewY());
                previewProjectionScreenController.setStageAndScene(stage2, scene2);
                scene2.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        stage2.setMaximized(!stage2.isMaximized());
                    }
                });
                stage2.setTitle(title);
                stage2.show();
                stage2.maximizedProperty().addListener((observable, oldValue, newValue) -> {
                });
                stage2.fullScreenProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue) {
                        stage2.setMaximized(false);
                    }
                });
                stage2.setOnCloseRequest(event -> {
                    final double width1 = stage2.getWidth();
                    final double height1 = stage2.getHeight();
                    final double x = stage2.getX();
                    final double y = stage2.getY();
                    if (x + width1 >= 0) {
                        settings.setPreviewX(x);
                        settings.setPreviewWidth(width1);
                    }
                    if (y + height1 >= 0) {
                        settings.setPreviewY(y);
                        settings.setPreviewHeight(height1);
                    }
                    settings.save();
                });
            } catch (IOException | NullPointerException e) {
                LOG.error(e.getMessage(), e);
            }
        } else {
            final Stage stage = previewProjectionScreenController.getStage();
            stage.setX(settings.getPreviewX());
            stage.setY(settings.getPreviewY());
            stage.setWidth(settings.getPreviewWidth());
            stage.setHeight(settings.getPreviewHeight());
            stage.show();
        }
        if (previewProjectionScreenController != null) {
            previewProjectionScreenController.setText(activeText, projectionType, projectionData);
        }
    }

    private double getSceneAspectRatio(Scene scene) {
        if (scene != null) {
            double width = max(scene.getWidth(), 16);
            int height = max((int) scene.getHeight(), 9);
            return height / width;
        }
        double x = 9;
        return x / 16;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        setScene(stage.getScene());
        loadEmpty();
    }

    public void setStageAndScene(Stage stage, Scene scene) {
        this.stage = stage;
        setScene(scene);
        loadEmpty();
    }

    public void loadEmpty() {
        setText("", projectionType, projectionData);
        setBackGroundColor();
    }

    public void onClose() {
        if (previewProjectionScreenController != null) {
            final Stage stage2 = previewProjectionScreenController.getStage();
            if (stage2 != null) {
                final double width1 = stage2.getWidth();
                final double height1 = stage2.getHeight();
                final double x = stage2.getX();
                final double y = stage2.getY();
                if (x + width1 >= 0) {
                    settings.setPreviewX(x);
                    settings.setPreviewWidth(width1);
                }
                if (y + height1 >= 0) {
                    settings.setPreviewY(y);
                    settings.setPreviewHeight(height1);
                }
                settings.save();
                stage2.close();
            }
            previewProjectionScreenController.onClose();
        }
        countDownTimerRunning = false;
        getExecutorService().shutdown();
        closeStage();
    }

    private void closeStage() {
        Stage stage = getStage();
        if (stage != null) {
            stage.close();
        }
    }

    private void setParentProjectionScreenController(ProjectionScreenController parentProjectionScreenController) {
        this.parentProjectionScreenController = parentProjectionScreenController;
    }

    private void setDoubleProjectionScreenController(ProjectionScreenController doubleProjectionScreenController) {
        this.doubleProjectionScreenController = doubleProjectionScreenController;
    }

    public void setLock(boolean selected) {
        isLock = selected;
        Stage stage = null;
        if (previewProjectionScreenController != null) {
            stage = previewProjectionScreenController.getStage();
        }
        if (!isLock) {
            repaint();
            if (stage != null) {
                setOpacityAndTitle(stage, 1, "Preview");
            }
        } else {
            if (stage != null) {
                setOpacityAndTitle(stage, 0.77, "Preview (MAIN LOCKED)");
            }
        }
        for (OnBlankListener onLockListener : onLockListeners) {
            onLockListener.onBlankChanged(isLock);
        }
    }

    public boolean isLock() {
        return isLock;
    }

    void setPrimaryStage(Stage primaryStage) {
        broughtToTheFront = false;
        primaryStage.focusedProperty().addListener((observable, oldValue, newValue) -> {
            //                System.out.println(newValue);
            if (newValue) {
                if (broughtToTheFront) {
                    broughtToTheFront = false;
                } else {
                    broughtToTheFront = true;
                    Thread thread = new Thread(() -> {
                        try {
                            sleep(7);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                        Platform.runLater(() -> {
                            try {
                                if (previewProjectionScreenController != null) {
                                    previewProjectionScreenController.getStage().toFront();
                                    primaryStage.toFront();
                                    primaryStage.requestFocus();
                                }
                            } catch (Exception e) {
                                LOG.error(e.getMessage(), e);
                            }
                        });
                    });
                    thread.start();
                }
            }
        });
    }

    public void setHeight(double height) {
        mainPane.setPrefHeight(height);
    }

    public void setWidth(double width) {
        mainPane.setPrefWidth(width);
    }

    public void setClip(double canvasWidth, double canvasHeight) {
        Rectangle clipRect = new Rectangle();
        clipRect.setWidth(canvasWidth);
        clipRect.setHeight(canvasHeight);
        mainPane.setClip(clipRect);
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
        setProgressLineProperties();
    }

    public void setProgress(double size) {
        if (!isLock) {
            if (contentPane == null) {
                return;
            }
            if (!handleByProjectionScreenSettingsAction()) {
                setLineSizeMain(size);
            }
        }
    }

    private void setLineSizeMain(double size) {
        setProgressLineProperties();
        if (size == 0) {
            progressLine.setStrokeWidth(0);
        } else {
            setProgressLineStrokeWidth();
        }
        final double width = contentPane.getWidth();
        progressLine.setEndX(width * size);
    }

    private void setProgressLineProperties() {
        double progressLineThickness = projectionScreenSettings.getProgressLineThickness();
        progressLine.setStrokeLineCap(StrokeLineCap.BUTT);
        if (!projectionScreenSettings.isProgressLinePositionIsTop()) {
            double endY = contentPane.getHeight() - 1;
            progressLine.setStartY(endY - progressLineThickness / 2);
            progressLine.setEndY(endY - progressLineThickness / 2);
        } else {
            progressLine.setStartY(1 + progressLineThickness / 2);
            progressLine.setEndY(1 + progressLineThickness / 2);
        }
        if (progressLine.getStrokeWidth() > 0) {
            setProgressLineStrokeWidth();
        }
    }

    private void setProgressLineStrokeWidth() {
        progressLine.setStrokeWidth(projectionScreenSettings.getProgressLineThickness());
    }

    public synchronized void addProjectionImageChangeListener(ProjectionTextChangeListener projectionTextChangeListener) {
        if (projectionImageChangeListeners == null) {
            projectionImageChangeListeners = new ArrayList<>();
        }
        projectionImageChangeListeners.add(projectionTextChangeListener);
    }

    public void removeProjectionTextChangeListener(ProjectionTextChangeListener projectionTextChangeListener) {
        if (projectionImageChangeListeners != null) {
            Platform.runLater(() -> projectionImageChangeListeners.remove(projectionTextChangeListener));
        }
    }

    public ProjectionScreenSettings getProjectionScreenSettings() {
        return projectionScreenSettings;
    }

    public void setProjectionScreenSettings(ProjectionScreenSettings projectionScreenSettings) {
        this.projectionScreenSettings = projectionScreenSettings;
        initializeFromSettings();
    }

    public void onSettingsChanged() {
        repaint();
    }

    private void initializeMargins() {
        try {
            Insets margins = new Insets(
                    projectionScreenSettings.getTopMarginD(),
                    projectionScreenSettings.getRightMarginD(),
                    projectionScreenSettings.getBottomMarginD(),
                    projectionScreenSettings.getLeftMarginD()
            );
            BorderPane.setMargin(getPaneForMargins(), margins);
            BorderPane.setMargin(getPaneForResetMargins(), null);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private Node getPaneForResetMargins() {
        if (projectionScreenSettings.isAsPadding()) {
            return paneForMargins;
        }
        return paneForPadding;
    }

    private Pane getPaneForMargins() {
        if (projectionScreenSettings.isAsPadding()) {
            return paneForPadding;
        }
        return paneForMargins;
    }

    public Popup getPopup() {
        return popup;
    }

    public void setPopup(Popup popup) {
        this.popup = popup;
    }

    public Pane getRoot() {
        return root;
    }

    public void setRoot(Pane root) {
        this.root = root;
    }

    public void hidePopups() {
        hidePopup();
        if (doubleProjectionScreenController != null) {
            doubleProjectionScreenController.hidePopups();
        }
    }

    public void hidePopup() {
        Popup popup = getPopup();
        if (popup != null) {
            popup.hide();
        }
    }

    public void toggleBlank() {
        setBlankLocally(!isBlank);
    }

    public void toggleLock() {
        setLock(!isLock);
    }

    public void toggleShowHidePopup() {
        Popup popup = getPopup();
        if (popup == null) {
            return;
        }
        if (popup.isShowing()) {
            popup.hide();
        } else {
            mainDesktop.createPopupForNextScreen(screen, this);
        }
    }

    public Screen getScreen() {
        return screen;
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
    }

    public Stage getPrimaryStageVariable() {
        return primaryStage;
    }

    public void setPrimaryStageVariable(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setMainDesktop(MainDesktop mainDesktop) {
        this.mainDesktop = mainDesktop;
    }

    public void addViewChangedListener(ViewChangedListener viewChangedListener) {
        viewChangedListeners.add(viewChangedListener);
    }

    private void onViewChanged() {
        for (ViewChangedListener viewChangedListener : viewChangedListeners) {
            viewChangedListener.viewChanged();
        }
    }

    public BorderPane getMainPane() {
        return mainPane;
    }

    public void addOnBlankListener(OnBlankListener onBlankListener) {
        onBlankListeners.add(onBlankListener);
    }

    public void addOnLockListener(OnBlankListener onLockListener) {
        onLockListeners.add(onLockListener);
    }

    public boolean isSetTextCalled() {
        return setTextCalled;
    }

    public void close() {
        if (popup != null) {
            popup.hide();
            popup = null;
        }
    }

    public void setInitialDotText() {
        setText(INITIAL_DOT_TEXT, ProjectionType.REFERENCE, projectionData);
    }

    public void setImage(String fileImagePath, ProjectionType projectionType, String nextFileImagePath) {
        this.projectionType = projectionType;
        this.fileImagePath = fileImagePath;
        this.lastImage = null;
        if (!handleByProjectionScreenSettingsAction()) {
            setImageMain(nextFileImagePath);
        }
    }

    private void setImageMain(String nextFileImagePath) {
        ExecutorService executorService = getExecutorService();
        BackgroundTask backgroundTask = new BackgroundTask();
        executorService.submit(backgroundTask);
        if (nextFileImagePath != null) {
            executorService.submit(() -> {
                ScaledSizes scaledSizes = getGetScaledSizes();
                ImageCacheService.getInstance().checkForImage(nextFileImagePath, (int) scaledSizes.width(), (int) scaledSizes.height());
            });
        }
    }

    public void setGalleryController(GalleryController galleryController) {
        this.galleryController = galleryController;
    }

    public void updateProjectorState(ProjectorState projectorState) {
        projectorState.setProjectionType(projectionType);
        projectorState.setProjectionData(projectionData);
        projectorState.setActiveText(activeText);
    }

    public void setByProjectorState(ProjectorState projectorState) {
        String s = projectorState.getActiveText();
        ProjectionType stateProjectionType = projectorState.getProjectionType();
        if (s != null && stateProjectionType != null) {
            setText(s, stateProjectionType, projectorState.getProjectionData());
        }
    }

    private Image getImageForProjectorScreenController(String fileImagePath) {
        ScaledSizes scaledSizes = getGetScaledSizes();
        return ImageCacheService.getInstance().getImage(fileImagePath, (int) scaledSizes.width(), (int) scaledSizes.height());
    }

    private ScaledSizes getGetScaledSizes() {
        double scale = getScale();
        double width = contentPane.getWidth() * scale;
        double height = contentPane.getHeight() * scale;
        return new ScaledSizes(width, height);
    }

    public void stopCountDownTimer() {
        try {
            this.countDownTimerRunning = false;
            if (this.countDownTimerThread != null) {
                this.countDownTimerThread.interrupt();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public String getActiveText() {
        return activeText;
    }

    private record ScaledSizes(double width, double height) {
    }

    private double getScale() {
        double scale = getScreenScale(getProjectionScreenSettings().getProjectionScreenHolder(), screen);
        return max(scale, 0.5);
    }

    public void setBrightness(double brightness) {
        this.brightness = brightness;
    }

    public void setContrast(double contrast) {
        this.contrast = contrast;
    }

    public void setSaturation(double saturation) {
        this.saturation = saturation;
    }

    public class BackgroundTask implements Runnable {

        @Override
        public void run() {
            try {
                Image image = getImageForProjectorScreenController(ProjectionScreenController.this.fileImagePath);
                if (isLock) {
                    return; // load remains before this, to be eager
                }
                drawAnImageOnCanvas(image);
                callOnImageListeners(image);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private void callOnImageListeners(Image image) {
        if (projectionImageChangeListeners != null) {
            // because of concurrent modification exception!
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < projectionImageChangeListeners.size(); ++i) {
                ProjectionTextChangeListener projectionTextChangeListener = projectionImageChangeListeners.get(i);
                projectionTextChangeListener.onImageChanged(image, projectionType, projectionData);
            }
        }
    }

    public void drawImage(Image image) {
        this.projectionType = ProjectionType.IMAGE;
        this.lastImage = image;
        this.fileImagePath = null;
        if (isLock) {
            return;
        }
        drawAnImageOnCanvas(image);
    }

    private void drawAnImageOnCanvas(Image image) {
        if (image == null) {
            return;
        }
        loadEmpty();
        double width = contentPane.getWidth();
        double height = contentPane.getHeight();
        canvas.setWidth(width);
        canvas.setHeight(height);
        clearCanvas(canvas);
        this.image = image; // if we need to make adjustments later
        drawImageOnCanvasColorAdjustments(image, canvas);
        canvas.setVisible(true);
    }

    public static double getCircleYInterpretation(double x) {
        double radius = 1.0;
        if (x < -radius || x > radius) {
            LOG.warn("Input contrast must be in the range [-1, 1]. Was contrast: {}", x);
            //noinspection SuspiciousNameCombination
            return x;
        }
        // Calculate y for the upper half of the circle
        double yUpper = 1 - Math.sqrt(radius - x * x);
        // Calculate y for the lower half of the circle
        if (x >= 0) {
            return yUpper;
        } else {
            return -yUpper;
        }
    }

    public GraphicsContext getGraphicsContext(Canvas canvas) {
        // Create a Canvas with the same dimensions as the image
        GraphicsContext gc = canvas.getGraphicsContext2D();
        // gc.setTransform(1, 0, 0, 1, 0, 0); // The scale from os should be disabled for these images if we use getGetScaledSizes

        // Create a ColorAdjust object to adjust brightness
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(getCircleYInterpretation(brightness)); // Adjust the brightness value as needed (0.0 is no change)
        colorAdjust.setContrast(getCircleYInterpretation(contrast));
        colorAdjust.setSaturation(saturation);
        // Draw the adjusted image onto the canvas
        gc.setEffect(colorAdjust);
        return gc;
    }

    public void drawImageOnCanvasColorAdjustments(Image image, Canvas canvas) {
        if (image == null) {
            return;
        }
        GraphicsContext gc = getGraphicsContext(canvas);
        drawMiddle(image, canvas, gc);
    }

    public void redrawImageForAdjustment() {
        drawImageOnCanvasColorAdjustments(image, canvas);
    }

    public static void drawMiddle(Image image, Canvas canvas, GraphicsContext gc) {
        double width = image.getWidth();
        double height = image.getHeight();
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();
        double scaleFactor = Math.min(canvasWidth / width, canvasHeight / height);
        double scaledWidth = width * scaleFactor;
        double scaledHeight = height * scaleFactor;

        // Calculate the position to center the image on the canvas
        double x = (canvasWidth - scaledWidth) / 2;
        double y = (canvasHeight - scaledHeight) / 2;

        gc.drawImage(image, x, y, scaledWidth, scaledHeight);
    }
}
