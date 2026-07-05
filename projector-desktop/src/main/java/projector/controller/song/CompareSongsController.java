package projector.controller.song;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import projector.application.Settings;
import projector.model.Song;
import projector.utils.compare.CompareSongsSettings;
import projector.utils.compare.SongCompareEngine;
import projector.utils.compare.VerseCompareEntry;

import java.util.List;
import java.util.ResourceBundle;

public class CompareSongsController {

    @FXML
    private Label leftTitleLabel;
    @FXML
    private Label rightTitleLabel;
    @FXML
    private Label diffSummaryLabel;
    @FXML
    private TextFlow leftTextFlow;
    @FXML
    private TextFlow rightTextFlow;
    @FXML
    private ScrollPane leftScrollPane;
    @FXML
    private ScrollPane rightScrollPane;
    @FXML
    private CheckBox strictDiffCheckBox;
    @FXML
    private CheckBox ignoreCaseCheckBox;
    @FXML
    private CheckBox ignoreAccentsCheckBox;
    @FXML
    private CheckBox normalizeWhitespaceCheckBox;

    private final ResourceBundle resourceBundle = Settings.getInstance().getResourceBundle();
    private CompareSongsSettings settings;
    private Song originalSong;
    private Song changedSong;
    private Stage stage;
    private boolean syncingScroll;

    public void initialize() {
        settings = CompareSongsSettings.load();
        strictDiffCheckBox.setSelected(settings.isStrictDiff());
        ignoreCaseCheckBox.setSelected(settings.isIgnoreCase());
        ignoreAccentsCheckBox.setSelected(settings.isIgnoreAccents());
        normalizeWhitespaceCheckBox.setSelected(settings.isNormalizeWhitespace());
        strictDiffCheckBox.setOnAction(e -> onSettingsChanged());
        ignoreCaseCheckBox.setOnAction(e -> onSettingsChanged());
        ignoreAccentsCheckBox.setOnAction(e -> onSettingsChanged());
        normalizeWhitespaceCheckBox.setOnAction(e -> onSettingsChanged());
        updateSettingsEnabledState();
        bindLinkedScrolling();
    }

    public void setSongs(Song originalSong, Song changedSong) {
        this.originalSong = originalSong;
        this.changedSong = changedSong;
        if (originalSong != null) {
            leftTitleLabel.setText(resourceBundle.getString("Original") + ": " + originalSong.getTitle());
        }
        if (changedSong != null) {
            rightTitleLabel.setText(resourceBundle.getString("Your version") + ": " + changedSong.getTitle());
        }
        recalculateDiff();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void closeButtonOnAction() {
        if (stage != null) {
            stage.close();
        }
    }

    private void onSettingsChanged() {
        settings.setStrictDiff(strictDiffCheckBox.isSelected());
        settings.setIgnoreCase(ignoreCaseCheckBox.isSelected());
        settings.setIgnoreAccents(ignoreAccentsCheckBox.isSelected());
        settings.setNormalizeWhitespace(normalizeWhitespaceCheckBox.isSelected());
        settings.save();
        updateSettingsEnabledState();
        recalculateDiff();
    }

    private void updateSettingsEnabledState() {
        boolean strict = strictDiffCheckBox.isSelected();
        ignoreCaseCheckBox.setDisable(strict);
        ignoreAccentsCheckBox.setDisable(strict);
        normalizeWhitespaceCheckBox.setDisable(strict);
    }

    private void recalculateDiff() {
        if (originalSong == null || changedSong == null) {
            return;
        }
        String leftText = originalSong.getVersesText();
        String rightText = changedSong.getVersesText();
        SongCompareEngine.renderDiff(leftTextFlow, leftText, rightText, settings);
        SongCompareEngine.renderDiff(rightTextFlow, rightText, leftText, settings);
        leftScrollPane.setVvalue(0);
        rightScrollPane.setVvalue(0);
        updateSummaryLabel();
    }

    private void updateSummaryLabel() {
        List<VerseCompareEntry> verseEntries = SongCompareEngine.buildVerseEntries(originalSong, changedSong, settings);
        int contentDiffs = SongCompareEngine.countContentDifferences(verseEntries);
        int orderDiffs = SongCompareEngine.countOrderOnlyDifferences(verseEntries);
        boolean orderOnlySong = SongCompareEngine.isSectionOrderOnlyDifference(originalSong, changedSong, verseEntries);
        if (orderOnlySong && contentDiffs == 0) {
            diffSummaryLabel.setText(resourceBundle.getString("Compare order only sections"));
        } else if (contentDiffs == 0 && orderDiffs == 0) {
            diffSummaryLabel.setText(resourceBundle.getString("Compare no differences"));
        } else {
            StringBuilder summary = new StringBuilder();
            if (contentDiffs > 0) {
                summary.append(String.format(resourceBundle.getString("Compare content difference count"), contentDiffs));
            }
            if (orderDiffs > 0) {
                if (!summary.isEmpty()) {
                    summary.append(", ");
                }
                summary.append(String.format(resourceBundle.getString("Compare order difference count"), orderDiffs));
            }
            diffSummaryLabel.setText(summary.toString());
        }
    }

    private void bindLinkedScrolling() {
        leftScrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
            if (!syncingScroll) {
                syncingScroll = true;
                rightScrollPane.setVvalue(newValue.doubleValue());
                syncingScroll = false;
            }
        });
        rightScrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
            if (!syncingScroll) {
                syncingScroll = true;
                leftScrollPane.setVvalue(newValue.doubleValue());
                syncingScroll = false;
            }
        });
    }
}
