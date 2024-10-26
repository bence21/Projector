package projector.controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    public ComboBox<String> actionComboBox;
    public ComboBox<ProjectionScreenBunch> projectionScreensComboBox;
    public CheckBox showFinishTimeCheckBox;
    @FXML
    private Label countDownLabel;
    @FXML
    private TextField timeTextField;
    private final ProjectionScreensUtil projectionScreensUtil = ProjectionScreensUtil.getInstance();
    private ProjectionScreenController projectionScreenController;
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
    }

    private void initializeActionComboBox() {
        try {
            ResourceBundle resourceBundle = Settings.getInstance().getResourceBundle();
            actionComboBox.getItems().addAll(
                    "-",
                    resourceBundle.getString("Empty"),
                    resourceBundle.getString("Song title"));
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
            default -> null;
        };
    }

    private void loadCountdownTimes(boolean setFirst) {
        try {
            List<CountdownTime> countdownTimes = ServiceManager.getCountdownTimeService().findAll();
            if (!countdownTimes.isEmpty()) {
                sortCountdownTimes(countdownTimes);
                if (setFirst) {
                    fillWithSelected(countdownTimes.get(0));
                }
            }
            createCountdownTimesMenu(countdownTimes);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
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

    private void createCountdownTimesMenu(List<CountdownTime> countdownTimes) {
        timeTextField.setOnMouseClicked(event -> {
            sortCountdownTimes(countdownTimes);
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
        String timeTextFromDate = getTimeTextFromDate(remainedTime);
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
            return projectionScreenController;
        } else {
            return selectedProjectionScreenHolder.getProjectionScreenController();
        }
    }

    public void onShowCountDownButtonEvent() {
        AutomaticAction selectedAction = getSelectedAction();
        ProjectionScreenController selectedProjectionScreenController = getSelectedProjectionScreenController();
        boolean showFinishTime = showFinishTimeCheckBox.isSelected();
        if (selectedProjectionScreenController != null) {
            ProjectionScreenController mainProjectionController = MyController.getInstance().getProjectionScreenController();
            if (selectedProjectionScreenController == mainProjectionController) {
                projectionScreensUtil.stopOtherCountDownTimer();
            }
            projectionScreensUtil.setCountDownTimer(selectedProjectionScreenController, getFinishDate(), selectedAction, showFinishTime);
        } else {
            LOG.error("selectedProjectionScreenController is null");
        }
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

    public void setProjectionScreenController(ProjectionScreenController projectionScreenController) {
        this.projectionScreenController = projectionScreenController;
    }
}
