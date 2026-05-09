package projector.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.Settings;
import projector.controller.listener.ProjectionScreenListener;
import projector.controller.util.AutomaticAction;
import projector.controller.util.ProjectionScreenBunch;
import projector.controller.util.ProjectionScreenHolder;
import projector.controller.util.ProjectionScreensUtil;
import projector.model.CountdownTime;
import projector.service.CountdownTimeService;
import projector.service.ServiceManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ResourceBundle;

import static projector.utils.ContextMenuUtil.getDeleteMenuItem;
import static projector.utils.ContextMenuUtil.initializeContextMenu;
import static projector.utils.ContextMenuUtil.setContextMenuHideAction;
import static projector.utils.CountDownTimerUtil.getRemainedTime;
import static projector.utils.CountDownTimerUtil.getTimeTextFromDate;
import static projector.utils.KeyEventUtil.getTextFromEvent;

public class UtilsController {

    private static final Logger LOG = LoggerFactory.getLogger(UtilsController.class);
    public static final int MAX_LINES = 3;
    public ComboBox<String> actionComboBox;
    public ComboBox<ProjectionScreenBunch> projectionScreensComboBox;
    public CheckBox showFinishTimeCheckBox;
    @FXML
    private Label countDownLabel;
    @FXML
    private TextField timeTextField;
    @FXML
    private TextField qrTitleTextField;
    @FXML
    private TextArea qrDescriptionTextArea;
    @FXML
    private TextField qrContentTextField;
    @FXML
    private ImageView qrImageView;
    @FXML
    private Button saveQrPngButton;
    private BufferedImage lastQrBufferedImage;
    private final ProjectionScreensUtil projectionScreensUtil = ProjectionScreensUtil.getInstance();
    private MouseButton lastMouseButton = null;
    private MouseEvent lastMouseEvent = null;
    private ContextMenu deleteContextMenu = null;

    public void initialize() {
        initializeActionComboBox();
        initializeProjectionScreensComboBox();
        loadCountdownTimes(true);
        timeTextField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            String text = getTextFromEvent(event);
            if (!text.matches("[0-9]") && (timeTextField.getText().contains(":") && text.equals(":"))) {
                event.consume();
            }
        });
        timeTextField.setOnKeyReleased(event -> setCountDownValue());
        Thread thread = new Thread(() -> {
            try {
                Settings settings = Settings.getInstance();
                while (settings.isApplicationRunning()) {
                    setCountDownValue();
                    //noinspection BusyWait
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                LOG.error(e.getMessage(), e);
            }
        });
        thread.start();
        saveQrPngButton.setDisable(true);
    }

    public void onGenerateQrButtonEvent() {
        if (qrContentTextField == null) {
            return;
        }
        String text = qrContentTextField.getText() != null ? qrContentTextField.getText().trim() : "";
        if (text.isEmpty()) {
            qrImageView.setImage(null);
            lastQrBufferedImage = null;
            saveQrPngButton.setDisable(true);
            return;
        }
        try {
            int matrixSize = getQrMatrixSideLengthForCaptionedProjection();
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, matrixSize, matrixSize);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            String title = qrTitleTextField != null && qrTitleTextField.getText() != null ? qrTitleTextField.getText().trim() : "";
            String description = qrDescriptionTextArea != null && qrDescriptionTextArea.getText() != null ? qrDescriptionTextArea.getText().trim() : "";
            BufferedImage compositeImage = createQrCompositeImage(bufferedImage, title, description);

            lastQrBufferedImage = compositeImage;
            Image fxImage = SwingFXUtils.toFXImage(compositeImage, null);
            qrImageView.setImage(fxImage);
            saveQrPngButton.setDisable(false);
            projectionScreensUtil.drawImage(fxImage);
        } catch (WriterException e) {
            LOG.error(e.getMessage(), e);
            qrImageView.setImage(null);
            lastQrBufferedImage = null;
            saveQrPngButton.setDisable(true);
        }
    }

    private int getQrMatrixSideLengthForCaptionedProjection() {
        int side = projectionScreensUtil.getMaxQrEncodeSideLength();
        // Reserve some space for caption (projection will scale the final composite to fit the screen).
        // Keep a minimum size so QR stays scannable even with long captions.
        int scaled = (int) Math.round(side * 0.85);
        return Math.max(256, Math.min(side, scaled));
    }

    private static BufferedImage createQrCompositeImage(BufferedImage qrImage, String title, String description) {
        if (qrImage == null) {
            return null;
        }
        String safeTitle = title != null ? title.trim() : "";
        String safeDescription = description != null ? description.trim() : "";
        boolean hasCaption = !safeTitle.isEmpty() || !safeDescription.isEmpty();
        if (!hasCaption) {
            return qrImage;
        }

        int totalW = qrImage.getWidth();
        int qrH = qrImage.getHeight();

        int paddingX = Math.max(24, (int) Math.round(totalW * 0.05));
        int paddingY = Math.max(18, (int) Math.round(totalW * 0.04));
        int gap = Math.max(10, (int) Math.round(totalW * 0.02));

        int titleFontSize = clamp((int) Math.round(totalW * 0.06), 22, 64);
        int descFontSize = clamp((int) Math.round(totalW * 0.045), 18, 48);
        Font titleFont = new Font("SansSerif", Font.BOLD, titleFontSize);
        Font descFont = new Font("SansSerif", Font.PLAIN, descFontSize);

        BufferedImage measuringImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D mg = measuringImage.createGraphics();
        mg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        mg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int availableTextWidth = Math.max(1, totalW - paddingX * 2);
        int titleHeight = 0;
        if (!safeTitle.isEmpty()) {
            mg.setFont(titleFont);
            titleHeight = mg.getFontMetrics().getHeight();
        }

        int descLineHeight = 0;
        List<String> descLines = List.of();
        if (!safeDescription.isEmpty()) {
            mg.setFont(descFont);
            descLineHeight = mg.getFontMetrics().getHeight();
            descLines = wrapText(mg, safeDescription, availableTextWidth, MAX_LINES);
        }
        mg.dispose();

        int captionHeight = paddingY
                + (Math.max(titleHeight, 0))
                + (!safeTitle.isEmpty() && !descLines.isEmpty() ? (int) Math.round(descLineHeight * 0.15) : 0)
                + (descLines.size() * descLineHeight)
                + paddingY;

        int totalH = qrH + gap + captionHeight;

        BufferedImage out = new BufferedImage(totalW, totalH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, totalW, totalH);
        g.drawImage(qrImage, 0, 0, null);

        int y = qrH + gap + paddingY;
        g.setColor(Color.WHITE);

        if (!safeTitle.isEmpty()) {
            g.setFont(titleFont);
            y += g.getFontMetrics().getAscent();
            g.drawString(truncateWithEllipsis(g, safeTitle, availableTextWidth), paddingX, y);
            y += g.getFontMetrics().getDescent();
            y += (int) Math.round(titleFontSize * 0.20);
        }

        if (!descLines.isEmpty()) {
            g.setFont(descFont);
            for (String line : descLines) {
                y += g.getFontMetrics().getAscent();
                g.drawString(line, paddingX, y);
                y += g.getFontMetrics().getDescent();
            }
        }

        g.dispose();
        return out;
    }

    private static List<String> wrapText(Graphics2D g, String text, int maxWidth, @SuppressWarnings("SameParameterValue") int maxLines) {
        String normalized = text.replace("\r\n", "\n").replace('\r', '\n').trim();
        if (normalized.isEmpty()) {
            return List.of();
        }

        Deque<String> chunks = new ArrayDeque<>();
        for (String paragraph : normalized.split("\n")) {
            String p = paragraph.trim();
            if (!p.isEmpty()) {
                chunks.addLast(p);
            }
        }
        if (chunks.isEmpty()) {
            return List.of();
        }

        List<String> lines = new ArrayList<>();
        while (!chunks.isEmpty() && lines.size() < maxLines) {
            String chunk = chunks.removeFirst();
            if (chunk.isEmpty()) {
                continue;
            }

            // If it fits, take it.
            if (g.getFontMetrics().stringWidth(chunk) <= maxWidth) {
                lines.add(chunk);
                continue;
            }

            // Greedy word wrap.
            String[] words = chunk.split("\\s+");
            StringBuilder current = new StringBuilder();
            int idx = 0;
            while (idx < words.length && lines.size() < maxLines) {
                String word = words[idx];
                String candidate = current.isEmpty() ? word : current + " " + word;
                if (g.getFontMetrics().stringWidth(candidate) <= maxWidth) {
                    current.setLength(0);
                    current.append(candidate);
                    idx++;
                    continue;
                }

                if (current.isEmpty()) {
                    // Single very long token; hard cut with ellipsis.
                    lines.add(truncateWithEllipsis(g, word, maxWidth));
                    idx++;
                    break;
                } else {
                    lines.add(current.toString());
                    current.setLength(0);
                }
            }

            if (lines.size() < maxLines && !current.isEmpty()) {
                lines.add(current.toString());
            }

            if (lines.size() >= maxLines && idx < words.length) {
                // Put the remaining words back as one chunk for ellipsizing.
                StringBuilder remaining = new StringBuilder();
                for (int i = idx; i < words.length; i++) {
                    if (!remaining.isEmpty()) {
                        remaining.append(' ');
                    }
                    remaining.append(words[i]);
                }
                int last = lines.size() - 1;
                lines.set(last, truncateWithEllipsis(g, lines.get(last) + " " + remaining, maxWidth));
            }
        }

        if (lines.size() > maxLines) {
            return lines.subList(0, maxLines);
        }
        if (lines.size() == maxLines && !chunks.isEmpty()) {
            int last = lines.size() - 1;
            lines.set(last, truncateWithEllipsis(g, lines.get(last), maxWidth));
        }
        return lines;
    }

    private static String truncateWithEllipsis(Graphics2D g, String text, int maxWidth) {
        String s = text != null ? text.trim() : "";
        if (s.isEmpty()) {
            return "";
        }
        if (g.getFontMetrics().stringWidth(s) <= maxWidth) {
            return s;
        }
        String ellipsis = "…";
        int ellipsisWidth = g.getFontMetrics().stringWidth(ellipsis);
        if (ellipsisWidth >= maxWidth) {
            return ellipsis;
        }
        int end = s.length();
        while (end > 0) {
            String candidate = s.substring(0, end).trim() + ellipsis;
            if (g.getFontMetrics().stringWidth(candidate) <= maxWidth) {
                return candidate;
            }
            end--;
        }
        return ellipsis;
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    public void onSaveQrPngButtonEvent() {
        if (lastQrBufferedImage == null || qrContentTextField == null || qrContentTextField.getScene() == null) {
            return;
        }
        ResourceBundle resourceBundle = Settings.getInstance().getResourceBundle();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(resourceBundle.getString("SaveQrPng"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));

        setQrSaveFileChooserInitialDirectory(fileChooser);
        Stage stage = (Stage) qrContentTextField.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        if (!file.getName().toLowerCase().endsWith(".png")) {
            String name = file.getName() + ".png";
            File parent = file.getParentFile();
            file = parent != null ? new File(parent, name) : new File(name);
        }
        try {
            ImageIO.write(lastQrBufferedImage, "png", file);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void setQrSaveFileChooserInitialDirectory(FileChooser fileChooser) {
        File galleryDir = new File(GalleryController.FOLDER_PATH); // same root folder name as GalleryController
        if (galleryDir.exists() && galleryDir.isDirectory()) {
            fileChooser.setInitialDirectory(galleryDir);
            return;
        }
        fileChooser.setInitialDirectory(new File(".").getAbsoluteFile()); // fallback
    }

    private void initializeActionComboBox() {
        try {
            ResourceBundle resourceBundle = Settings.getInstance().getResourceBundle();
            actionComboBox.getItems().addAll(
                    "-",
                    resourceBundle.getString("Empty"),
                    resourceBundle.getString("Song title"),
                    resourceBundle.getString("Endless timer")
            );
            actionComboBox.getSelectionModel().select(0);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void initializeProjectionScreensComboBox() {
        try {
            ResourceBundle resourceBundle = Settings.getInstance().getResourceBundle();
            ObservableList<ProjectionScreenBunch> projectionScreensComboBoxItems = projectionScreensComboBox.getItems();
            ProjectionScreenBunch projectionScreenBunch = new ProjectionScreenBunch();
            projectionScreenBunch.setName(resourceBundle.getString("All"));
            projectionScreensComboBoxItems.add(projectionScreenBunch);
            handleProjectionScreensComboBox(projectionScreensComboBox);
            projectionScreensComboBox.getSelectionModel().select(0);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public static void handleProjectionScreensComboBox(ComboBox<ProjectionScreenBunch> projectionScreensComboBox) {
        handleProjectionScreensComboBox_(projectionScreensComboBox, false, null);
    }

    public static void handleProjectionScreensWithScreenComboBox(ComboBox<ProjectionScreenBunch> projectionScreensComboBox, ProjectionScreenHolder projectionScreenHolder) {
        handleProjectionScreensComboBox_(projectionScreensComboBox, true, projectionScreenHolder);
    }

    private static void handleProjectionScreensComboBox_(ComboBox<ProjectionScreenBunch> projectionScreensComboBox, boolean withScreen, ProjectionScreenHolder exceptProjectionScreenHolder) {
        try {
            ObservableList<ProjectionScreenBunch> projectionScreensComboBoxItems = projectionScreensComboBox.getItems();
            ProjectionScreensUtil projectionScreensUtil = ProjectionScreensUtil.getInstance();
            List<ProjectionScreenHolder> projectionScreenHolders = projectionScreensUtil.getProjectionScreenHolders();
            for (ProjectionScreenHolder projectionScreenHolder : projectionScreenHolders) {
                if (withScreenCondition(withScreen, projectionScreenHolder, exceptProjectionScreenHolder)) {
                    continue;
                }
                addProjectionScreenHolderToItems(projectionScreenHolder, projectionScreensComboBoxItems);
            }
            projectionScreensUtil.addProjectionScreenListener(new ProjectionScreenListener() {
                @Override
                public void onNew(ProjectionScreenHolder projectionScreenHolder) {
                    if (withScreenCondition(withScreen, projectionScreenHolder, exceptProjectionScreenHolder)) {
                        return;
                    }
                    addProjectionScreenHolderToItems(projectionScreenHolder, projectionScreensComboBoxItems);
                }

                @Override
                public void onRemoved(ProjectionScreenHolder projectionScreenHolder) {
                    if (projectionScreenHolder == null) {
                        return;
                    }
                    for (int i = 0; i < projectionScreensComboBoxItems.size(); ++i) {
                        ProjectionScreenBunch bunch = projectionScreensComboBoxItems.get(i);
                        if (projectionScreenHolder.equals(bunch.getProjectionScreenHolder())) {
                            projectionScreensComboBoxItems.remove(i);
                            break;
                        }
                    }
                }
            });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private static boolean withScreenCondition(boolean withScreen, ProjectionScreenHolder projectionScreenHolder, ProjectionScreenHolder exceptProjectionScreenHolder) {
        return withScreen && (projectionScreenHolder.isNotWithScreen() || projectionScreenHolder.equals(exceptProjectionScreenHolder));
    }

    public static void addProjectionScreenHolderToItems(ProjectionScreenHolder projectionScreenHolder, ObservableList<ProjectionScreenBunch> projectionScreensComboBoxItems) {
        ProjectionScreenBunch projectionScreenBunch = new ProjectionScreenBunch();
        projectionScreenBunch.setProjectionScreenHolder(projectionScreenHolder);
        projectionScreensComboBoxItems.add(projectionScreenBunch);
    }

    private AutomaticAction getSelectedAction() {
        return switch (actionComboBox.getSelectionModel().getSelectedIndex()) {
            case 0 -> AutomaticAction.NOTHING;
            case 1 -> AutomaticAction.EMPTY;
            case 2 -> AutomaticAction.SONG_TITLE;
            case 3 -> AutomaticAction.COUNTDOWN_TIMER_ENDLESS;
            default -> null;
        };
    }

    private void loadCountdownTimes(boolean setFirst) {
        try {
            List<CountdownTime> countdownTimes = getCountdownTimes();
            if (!countdownTimes.isEmpty()) {
                if (setFirst) {
                    fillWithSelected(countdownTimes.get(0));
                }
            }
            createCountdownTimesMenu();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private static List<CountdownTime> getCountdownTimes() {
        List<CountdownTime> countdownTimes = ServiceManager.getCountdownTimeService().findAll();
        sortCountdownTimes(countdownTimes);
        return countdownTimes;
    }

    private static void sortCountdownTimes(List<CountdownTime> countdownTimes) {
        Date now = new Date();
        long maximumDiff = getMaximumDifference(countdownTimes, now);
        long maximumCounter = getMaximumCounter(countdownTimes);
        countdownTimes.sort((o1, o2) -> {
            boolean expired1 = isExpired(o1, now);
            boolean expired2 = isExpired(o2, now);
            if (expired1) {
                if (!expired2) {
                    return 1;
                }
            } else {
                if (expired2) {
                    return -1;
                }
            }
            return Double.compare(o2.getScore(now, maximumDiff, maximumCounter), o1.getScore(now, maximumDiff, maximumCounter));
        });
    }

    private static long getMaximumCounter(List<CountdownTime> countdownTimes) {
        long max = 0;
        for (CountdownTime countdownTime : countdownTimes) {
            if (countdownTime.getCounter() > max) {
                max = countdownTime.getCounter();
            }
        }
        return max;
    }

    private static long getMaximumDifference(List<CountdownTime> countdownTimes, Date now) {
        long maximumDiff = 0;
        long nowTime = now.getTime();
        for (CountdownTime countdownTime : countdownTimes) {
            Date date = countdownTime.getDate();
            long diff = date.getTime() - nowTime;
            if (diff > maximumDiff) {
                maximumDiff = diff;
            }
        }
        return maximumDiff;
    }

    private static boolean isExpired(CountdownTime countdownTime, Date now) {
        Date dateByTimeText = countdownTime.getDate();
        if (dateByTimeText == null) {
            return true;
        }
        return dateByTimeText.before(now);
    }

    private void fillWithSelected(CountdownTime countdownTime) {
        timeTextField.setText(countdownTime.getTimeText());
        actionComboBox.getSelectionModel().select(countdownTime.getSelectedAction().ordinal());
        showFinishTimeCheckBox.setSelected(countdownTime.isShowFinishTime());
        selectProjectionScreensComboBoxItemByName(countdownTime.getSelectedProjectionScreenName());
    }

    private void selectProjectionScreensComboBoxItemByName(String selectedProjectionScreenName) {
        SingleSelectionModel<ProjectionScreenBunch> selectionModel = projectionScreensComboBox.getSelectionModel();
        if (selectedProjectionScreenName == null) {
            selectionModel.selectFirst();
        } else {
            ObservableList<ProjectionScreenBunch> projectionScreensComboBoxItems = projectionScreensComboBox.getItems();
            for (int i = 1; i < projectionScreensComboBoxItems.size(); ++i) {
                ProjectionScreenBunch projectionScreenBunch = projectionScreensComboBoxItems.get(i);
                if (selectedProjectionScreenName.equals(projectionScreenBunch.toString())) {
                    selectionModel.select(i);
                    break;
                }
            }
        }
    }

    private void createCountdownTimesMenu() {
        timeTextField.setOnMouseClicked(event -> {
            List<CountdownTime> countdownTimes = getCountdownTimes();
            ContextMenu contextMenu = createContextMenu(countdownTimes);
            contextMenu.show(timeTextField, Side.BOTTOM, 0, 0);
            hideDeleteContextMenu();
        });
    }

    private ContextMenu createContextMenu(List<CountdownTime> countdownTimes) {
        final ContextMenu contextMenu = new ContextMenu();
        setContextMenuHideAction(contextMenu, LOG);
        List<MenuItem> menuItems = new ArrayList<>();
        contextMenu.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            lastMouseEvent = event;
            lastMouseButton = event.getButton();
        });
        for (CountdownTime countdownTime : countdownTimes) {
            MenuItem menuItem = new MenuItem(countdownTime.getTimeText());
            menuItem.setOnAction(event -> {
                if (isLastRightClick()) {
                    createDeleteMenu(countdownTime, contextMenu, menuItem);
                } else {
                    fillWithSelected(countdownTime);
                }
            });
            menuItems.add(menuItem);
        }
        contextMenu.getItems().addAll(menuItems);
        return contextMenu;
    }

    private void createDeleteMenu(CountdownTime countdownTime, ContextMenu mainContextMenu, MenuItem menuItem) {
        if (lastMouseEvent == null) {
            return;
        }
        hideDeleteContextMenu();
        final ContextMenu contextMenu = new ContextMenu();
        deleteContextMenu = contextMenu;
        initializeContextMenu(contextMenu, LOG);
        MenuItem deleteMenuItem = getDeleteMenuItem();
        deleteMenuItem.setText(deleteMenuItem.getText() + " - " + countdownTime.getTimeText());
        deleteMenuItem.setOnAction(event -> {
            mainContextMenu.getItems().remove(menuItem);
            ServiceManager.getCountdownTimeService().delete(countdownTime);
        });
        contextMenu.getItems().addAll(deleteMenuItem,
                new MenuItem(Settings.getInstance().getResourceBundle().getString("Cancel")));
        contextMenu.show(timeTextField, lastMouseEvent.getScreenX(), lastMouseEvent.getScreenY());
    }

    private void hideDeleteContextMenu() {
        if (deleteContextMenu != null) {
            deleteContextMenu.hide();
        }
    }

    private boolean isLastRightClick() {
        return lastMouseButton != null && lastMouseButton == MouseButton.SECONDARY;
    }

    private void setCountDownValue() {
        Long remainedTime = getRemainedTime(getFinishDate());
        String timeTextFromDate = getTimeTextFromDate(remainedTime, getSelectedAction());
        if (!timeTextFromDate.isEmpty() && !countDownLabel.getText().equals(timeTextFromDate)) {
            Platform.runLater(() -> countDownLabel.setText(timeTextFromDate));
        }
    }

    private Date getFinishDate() {
        try {
            String timeTextFieldText = getTimeTextFieldText();
            return getDateByTimeText(timeTextFieldText);
        } catch (Exception e) {
            return null;
        }
    }

    public static Date getDateByTimeText(String timeTextFieldText) {
        try {
            String[] split = timeTextFieldText.split(":");
            if (split.length < 2) {
                return null;
            }
            long hour = Integer.parseInt(split[0]);
            long minute = Integer.parseInt(split[1]);
            Date now = new Date();
            Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
            calendar.setTime(now);   // assigns calendar to given date
            long hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
            long minuteOfHour = calendar.get(Calendar.MINUTE);
            long secondsOfMinute = calendar.get(Calendar.SECOND);
            long millisecondsTo = (((hour - hourOfDay) * 60 - minuteOfHour + minute) * 60 - secondsOfMinute) * 1000;
            return new Date(now.getTime() + millisecondsTo);
        } catch (Exception e) {
            return null;
        }
    }

    private String getTimeTextFieldText() {
        return timeTextField.getText().trim();
    }

    private ProjectionScreenController getSelectedProjectionScreenController() {
        ProjectionScreenHolder selectedProjectionScreenHolder = getSelectedProjectionScreenHolder();
        if (selectedProjectionScreenHolder == null) {
            return null;
        } else {
            return selectedProjectionScreenHolder.getProjectionScreenController();
        }
    }

    public void onShowCountDownButtonEvent() {
        AutomaticAction selectedAction = getSelectedAction();
        ProjectionScreenController selectedProjectionScreenController = getSelectedProjectionScreenController();
        boolean showFinishTime = showFinishTimeCheckBox.isSelected();
        projectionScreensUtil.setCountDownTimer(selectedProjectionScreenController, getFinishDate(), selectedAction, showFinishTime);
        String timeText = getTimeTextFieldText();
        CountdownTimeService countdownTimeService = ServiceManager.getCountdownTimeService();
        List<CountdownTime> countdownTimes = countdownTimeService.findAll();
        CountdownTime countdownTime = findCountdownTimeByTimeText(countdownTimes, timeText);
        countdownTime.setCounter(countdownTime.getCounter() + 1);
        countdownTime.setSelectedAction(selectedAction);
        countdownTime.setShowFinishTime(showFinishTime);
        countdownTime.setSelectedProjectionScreenName(getSelectedProjectionScreenName());
        countdownTimeService.create(countdownTime);
        loadCountdownTimes(false);
    }

    private String getSelectedProjectionScreenName() {
        try {
            ProjectionScreenHolder selectedProjectionScreenHolder = getSelectedProjectionScreenHolder();
            if (selectedProjectionScreenHolder == null) {
                return null;
            }
            return getProjectionScreenBunch().toString();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    private ProjectionScreenHolder getSelectedProjectionScreenHolder() {
        try {
            ProjectionScreenBunch projectionScreenBunch = getProjectionScreenBunch();
            return projectionScreenBunch.getProjectionScreenHolder();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    private ProjectionScreenBunch getProjectionScreenBunch() {
        return projectionScreensComboBox.getSelectionModel().getSelectedItem();
    }

    private CountdownTime findCountdownTimeByTimeText(List<CountdownTime> countdownTimes, String timeText) {
        for (CountdownTime countdownTime : countdownTimes) {
            if (countdownTime.getTimeText().equals(timeText)) {
                return countdownTime;
            }
        }
        CountdownTime countdownTime = new CountdownTime();
        countdownTime.setTimeText(timeText);
        return countdownTime;
    }
}
