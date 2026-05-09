package projector.controller.song;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import projector.application.Settings;
import projector.controller.song.util.SchedulePasteEntry;
import projector.controller.song.util.SchedulePasteParser;
import projector.controller.song.util.ScheduleSongMatcher;
import projector.controller.song.util.TitleSimilarity;
import projector.model.Song;
import projector.utils.SceneUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Dialog to paste service text, preview matches, then append or replace the song schedule.
 */
public final class SchedulePasteImportDialog {
    private static final String FILTER_LISTENER_KEY = "scheduleImportFilterListener";
    private static final String FILTER_UPDATING_KEY = "scheduleImportFilterUpdating";

    private SchedulePasteImportDialog() {
    }

    private static void setFilterUpdating(ComboBox<Song> combo, boolean updating) {
        combo.getProperties().put(FILTER_UPDATING_KEY, updating);
    }

    private static boolean isFilterUpdating(ComboBox<Song> combo) {
        return Boolean.TRUE.equals(combo.getProperties().get(FILTER_UPDATING_KEY));
    }

    private static void runWithFilterGuard(ComboBox<Song> combo, Runnable action) {
        if (isFilterUpdating(combo)) {
            return;
        }
        setFilterUpdating(combo, true);
        try {
            action.run();
        } finally {
            setFilterUpdating(combo, false);
        }
    }

    private static boolean isAmbiguousStatus(PreviewRow row) {
        return row != null
                && row.getEntry().getKind() == SchedulePasteEntry.Kind.SONG
                && row.getMatchedSong() != null
                && row.getMatchConfidence() < ScheduleSongMatcher.AUTO_MATCH_THRESHOLD;
    }

    private static void applyStatusStyle(TableCell<PreviewRow, String> cell, PreviewRow row) {
        if (isAmbiguousStatus(row)) {
            cell.setStyle("-fx-text-fill: #ffb347; -fx-font-weight: bold;");
        } else {
            cell.setStyle("");
        }
    }

    private static void applyConfidenceStyle(TableCell<PreviewRow, Double> cell, PreviewRow row) {
        if (row == null || row.getEntry().getKind() != SchedulePasteEntry.Kind.SONG) {
            cell.setStyle("");
            return;
        }
        if (row.getMatchedSong() == null) {
            cell.setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold;");
            return;
        }
        if (row.getMatchConfidence() < ScheduleSongMatcher.AUTO_MATCH_THRESHOLD) {
            cell.setStyle("-fx-text-fill: #ffb347; -fx-font-weight: bold;");
            return;
        }
        cell.setStyle("");
    }

    private static void configureStatusColumn(TableColumn<PreviewRow, String> statusCol) {
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setAlignment(Pos.CENTER_LEFT);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                PreviewRow row = getTableRow() == null ? null : getTableRow().getItem();
                applyStatusStyle(this, row);
            }
        });
    }

    private static void updateComboItems(ComboBox<Song> combo, PreviewRow row, String filterText) {
        String normalizedFilter = TitleSimilarity.normalize(filterText);
        List<Song> ranked = row.getRankedSongs();
        if (normalizedFilter.isEmpty()) {
            combo.setItems(FXCollections.observableArrayList(ranked));
            return;
        }
        List<Song> filtered = new ArrayList<>();
        for (Song song : ranked) {
            String t = song.getStrippedTitle();
            if (t == null || t.isBlank()) {
                t = song.getTitle();
            }
            if (TitleSimilarity.normalize(t).contains(normalizedFilter)) {
                filtered.add(song);
            }
        }
        filtered.sort(Comparator.comparingDouble((Song s) -> TitleSimilarity.score(row.getParsedText(), s)).reversed());
        combo.setItems(FXCollections.observableArrayList(filtered));
    }

    @SuppressWarnings("unchecked")
    private static void bindComboEditorFilter(ComboBox<Song> combo, PreviewRow row) {
        ChangeListener<String> previous = (ChangeListener<String>) combo.getProperties().get(FILTER_LISTENER_KEY);
        if (previous != null) {
            combo.getEditor().textProperty().removeListener(previous);
        }
        ChangeListener<String> listener = (obs, oldValue, newValue) -> {
            if (isFilterUpdating(combo)) {
                return;
            }
            row.setFilterText(newValue == null ? "" : newValue);
            runWithFilterGuard(combo, () -> {
                updateComboItems(combo, row, row.getFilterText());
                boolean userEditingThisCombo = combo.isFocused() || combo.getEditor().isFocused();
                if (userEditingThisCombo && !combo.isShowing()) {
                    combo.show();
                }
            });
        };
        combo.getProperties().put(FILTER_LISTENER_KEY, listener);
        combo.getEditor().textProperty().addListener(listener);
    }

    private record TableUi(TableView<PreviewRow> table, TableColumn<PreviewRow, Song> songCol) {
    }

    private record NotFoundUi(VBox box, ListView<String> list) {
    }

    private static TextArea createClipboardTextArea() {
        TextArea textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.setPrefRowCount(8);
        javafx.scene.input.Clipboard cb = javafx.scene.input.Clipboard.getSystemClipboard();
        if (cb.hasString()) {
            String s = cb.getString();
            if (s != null && !s.isBlank()) {
                textArea.setText(s);
            }
        }
        return textArea;
    }

    private static HBox createTopBar(Button reparseButton, ProgressBar parseProgressBar, Label parseProgressLabel) {
        HBox topBar = new HBox(8, reparseButton, parseProgressBar, parseProgressLabel);
        topBar.setPadding(new Insets(4));
        topBar.setAlignment(Pos.CENTER_LEFT);
        return topBar;
    }

    private static TableUi createTableUi(ResourceBundle bundle) {
        TableView<PreviewRow> table = new TableView<>();
        TableColumn<PreviewRow, Integer> numCol = new TableColumn<>("#");
        numCol.setCellValueFactory(new PropertyValueFactory<>("rowNumber"));
        numCol.setPrefWidth(40);
        numCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<PreviewRow, String> textCol = new TableColumn<>(bundle.getString("Parsed text"));
        textCol.setCellValueFactory(new PropertyValueFactory<>("parsedText"));
        textCol.setPrefWidth(220);
        textCol.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<PreviewRow, String> statusCol = new TableColumn<>(bundle.getString("Status"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("statusText"));
        statusCol.setPrefWidth(100);
        configureStatusColumn(statusCol);

        TableColumn<PreviewRow, Double> confidenceCol = new TableColumn<>(bundle.getString("Match confidence"));
        confidenceCol.setCellValueFactory(new PropertyValueFactory<>("matchConfidence"));
        confidenceCol.setPrefWidth(120);
        confidenceCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setAlignment(Pos.CENTER);
                if (empty || item == null || item < 0) {
                    setText("—");
                    setStyle("");
                } else {
                    setText(String.format(Locale.ROOT, "%.0f%%", item * 100.0));
                    PreviewRow row = getTableRow() == null ? null : getTableRow().getItem();
                    applyConfidenceStyle(this, row);
                }
            }
        });

        TableColumn<PreviewRow, Song> songCol = new TableColumn<>(bundle.getString("Matched song"));
        songCol.setCellValueFactory(new PropertyValueFactory<>("matchedSong"));
        songCol.setPrefWidth(260);

        //noinspection unchecked
        table.getColumns().setAll(numCol, textCol, songCol, statusCol, confidenceCol);
        return new TableUi(table, songCol);
    }

    private static NotFoundUi createNotFoundUi(ResourceBundle bundle) {
        ListView<String> notFoundList = new ListView<>();
        notFoundList.setPrefHeight(96);
        VBox notFoundBox = new VBox(4);
        Label notFoundTitle = new Label(bundle.getString("Not in library"));
        notFoundBox.getChildren().add(notFoundTitle);
        notFoundBox.getChildren().add(notFoundList);
        Button copyNotFoundButton = new Button(bundle.getString("Copy list"));
        copyNotFoundButton.setOnAction(e -> {
            String joined = String.join(System.lineSeparator(), notFoundList.getItems());
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(joined);
            javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);
        });
        notFoundBox.getChildren().add(copyNotFoundButton);
        notFoundBox.setPadding(new Insets(8));
        notFoundBox.setVisible(false);
        notFoundBox.managedProperty().bind(notFoundBox.visibleProperty());
        return new NotFoundUi(notFoundBox, notFoundList);
    }

    private static Runnable createRefreshNotFoundRunnable(TableView<PreviewRow> table, NotFoundUi notFoundUi) {
        return () -> {
            ObservableList<String> nf = FXCollections.observableArrayList();
            for (PreviewRow r : table.getItems()) {
                if (r.getEntry().getKind() == SchedulePasteEntry.Kind.SONG && r.getMatchedSong() == null) {
                    nf.add(r.getEntry().getText());
                }
            }
            notFoundUi.list.setItems(nf);
            notFoundUi.box.setVisible(!nf.isEmpty());
        };
    }

    private static void configureSongColumn(TableColumn<PreviewRow, Song> songCol,
                                            TableView<PreviewRow> table, Runnable refreshNotFound) {
        songCol.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<Song> combo = new ComboBox<>();

            {
                combo.setEditable(true);
                combo.setConverter(new StringConverter<>() {
                    @Override
                    public String toString(Song s) {
                        return s == null ? "" : s.getTitle();
                    }

                    @Override
                    public Song fromString(String string) {
                        return null;
                    }
                });
            }

            @Override
            protected void updateItem(Song item, boolean empty) {
                super.updateItem(item, empty);
                combo.setOnAction(null);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                TableRow<PreviewRow> tr = getTableRow();
                if (tr == null || tr.getItem() == null) {
                    setGraphic(null);
                    return;
                }
                PreviewRow row = tr.getItem();
                if (row.getEntry().getKind() == SchedulePasteEntry.Kind.SECTION) {
                    setGraphic(null);
                    return;
                }
                runWithFilterGuard(combo, () -> {
                    updateComboItems(combo, row, row.getFilterText());
                    combo.setValue(row.getMatchedSong());
                    combo.getEditor().setText(row.getFilterText());
                });
                bindComboEditorFilter(combo, row);
                combo.setOnAction(e -> {
                    TableRow<PreviewRow> row1 = getTableRow();
                    if (row1 == null || row1.getItem() == null) {
                        return;
                    }
                    PreviewRow pr = row1.getItem();
                    pr.setMatchedSong(combo.getValue());
                    pr.refreshStatusText(pr.getMatchConfidence());
                    table.refresh();
                    refreshNotFound.run();
                });
                combo.setPrefWidth(240);
                setGraphic(combo);
            }
        });
    }

    private static HBox createButtonsBar(Button appendButton, Button replaceButton, Button cancelButton) {
        HBox buttons = new HBox(8, appendButton, replaceButton, cancelButton);
        buttons.setPadding(new Insets(8));
        buttons.setAlignment(Pos.CENTER_LEFT);
        return buttons;
    }

    private static void setIdleUi(ProgressBar parseProgressBar, Label parseProgressLabel, Button reparseButton,
                                  Button appendButton, Button replaceButton) {
        parseProgressBar.progressProperty().unbind();
        parseProgressLabel.textProperty().unbind();
        SceneUtils.setVisibleAndManaged(parseProgressBar, false);
        SceneUtils.setVisibleAndManaged(parseProgressLabel, false);
        reparseButton.setDisable(false);
        appendButton.setDisable(false);
        replaceButton.setDisable(false);
    }

    private record ReparseContext(TextArea textArea, SongController songController,
                                  TableView<PreviewRow> table, Runnable refreshNotFound,
                                  ProgressBar parseProgressBar, Label parseProgressLabel,
                                  Button reparseButton, Button appendButton, Button replaceButton,
                                  ResourceBundle bundle,
                                  Task<ObservableList<PreviewRow>>[] currentReparseTask,
                                  long[] reparseRequestId) {
    }

    private static Runnable createLaunchReparseRunnable(TextArea textArea, SongController songController,
                                                        TableView<PreviewRow> table, Runnable refreshNotFound,
                                                        ProgressBar parseProgressBar, Label parseProgressLabel,
                                                        Button reparseButton, Button appendButton,
                                                        Button replaceButton,
                                                        ResourceBundle bundle,
                                                        Task<ObservableList<PreviewRow>>[] currentReparseTask,
                                                        long[] reparseRequestId) {
        ReparseContext ctx = new ReparseContext(textArea, songController, table, refreshNotFound,
                parseProgressBar, parseProgressLabel, reparseButton, appendButton, replaceButton,
                bundle, currentReparseTask, reparseRequestId);
        return () -> launchReparse(ctx);
    }

    private static void launchReparse(ReparseContext ctx) {
        String rawText = ctx.textArea().getText();
        if (rawText == null) {
            rawText = "";
        }
        cancelRunningReparse(ctx);
        if (rawText.isBlank()) {
            clearPreview(ctx);
            return;
        }

        long requestId = ++ctx.reparseRequestId()[0];
        List<Song> songs = new ArrayList<>(ctx.songController().getSongsForScheduleImport());
        ObservableList<PreviewRow> liveRows = FXCollections.observableArrayList();
        ctx.table().setItems(liveRows);
        ctx.refreshNotFound().run();

        Task<ObservableList<PreviewRow>> task = createParseTask(ctx, rawText, songs, liveRows, requestId);
        ctx.currentReparseTask()[0] = task;
        setParsingUi(ctx, task);
        installTaskHandlers(ctx, task, requestId);
        startParseThread(task);
    }

    private static void cancelRunningReparse(ReparseContext ctx) {
        Task<ObservableList<PreviewRow>> previous = ctx.currentReparseTask()[0];
        if (previous != null && previous.isRunning()) {
            previous.cancel();
        }
    }

    private static void clearPreview(ReparseContext ctx) {
        ctx.table().setItems(FXCollections.observableArrayList());
        ctx.refreshNotFound().run();
        setIdleUi(ctx.parseProgressBar(), ctx.parseProgressLabel(), ctx.reparseButton(),
                ctx.appendButton(), ctx.replaceButton());
    }

    private static Task<ObservableList<PreviewRow>> createParseTask(ReparseContext ctx, String textToParse,
                                                                    List<Song> songs,
                                                                    ObservableList<PreviewRow> liveRows,
                                                                    long requestId) {
        return new Task<>() {
            @Override
            protected ObservableList<PreviewRow> call() {
                updateProgress(0, 1);
                updateMessage("Parsing...");
                List<SchedulePasteEntry> entries = SchedulePasteParser.parse(textToParse);
                int total = entries.isEmpty() ? 1 : entries.size();
                ObservableList<PreviewRow> rows = FXCollections.observableArrayList();
                int n = 1;
                int done = 0;
                for (SchedulePasteEntry entry : entries) {
                    if (isCancelled()) {
                        return rows;
                    }
                    PreviewRow previewRow = buildPreviewRow(entry, n++, songs, ctx.bundle());
                    rows.add(previewRow);
                    Platform.runLater(() -> {
                        if (requestId != ctx.reparseRequestId()[0]) {
                            return;
                        }
                        liveRows.add(previewRow);
                        ctx.refreshNotFound().run();
                    });
                    done++;
                    updateProgress(done, total);
                    updateMessage("Matching " + done + "/" + total);
                }
                return rows;
            }
        };
    }

    private static PreviewRow buildPreviewRow(SchedulePasteEntry entry, int rowNumber,
                                              List<Song> songs, ResourceBundle bundle) {
        if (entry.getKind() == SchedulePasteEntry.Kind.SECTION) {
            return PreviewRow.section(rowNumber, entry, bundle);
        }
        ScheduleSongMatcher.Result result = ScheduleSongMatcher.match(entry.getText(), songs);
        return PreviewRow.song(rowNumber, entry, result, songs, bundle);
    }

    private static void setParsingUi(ReparseContext ctx, Task<?> task) {
        ctx.reparseButton().setDisable(true);
        ctx.appendButton().setDisable(true);
        ctx.replaceButton().setDisable(true);
        SceneUtils.setVisibleAndManaged(ctx.parseProgressBar(), true);
        SceneUtils.setVisibleAndManaged(ctx.parseProgressLabel(), true);
        ctx.parseProgressBar().progressProperty().bind(task.progressProperty());
        ctx.parseProgressLabel().textProperty().bind(task.messageProperty());
    }

    private static void setFailedUi(ReparseContext ctx) {
        ctx.parseProgressBar().progressProperty().unbind();
        ctx.parseProgressLabel().textProperty().unbind();
        SceneUtils.setVisibleAndManaged(ctx.parseProgressBar(), true);
        ctx.parseProgressBar().setProgress(0);
        SceneUtils.setVisibleAndManaged(ctx.parseProgressLabel(), true);
        ctx.parseProgressLabel().setText("Failed to parse");
        ctx.reparseButton().setDisable(false);
        ctx.appendButton().setDisable(false);
        ctx.replaceButton().setDisable(false);
    }

    private static void installTaskHandlers(ReparseContext ctx, Task<ObservableList<PreviewRow>> task,
                                            long requestId) {
        task.setOnSucceeded(event -> {
            if (requestId != ctx.reparseRequestId()[0]) {
                return;
            }
            ctx.refreshNotFound().run();
            setIdleUi(ctx.parseProgressBar(), ctx.parseProgressLabel(), ctx.reparseButton(),
                    ctx.appendButton(), ctx.replaceButton());
        });
        task.setOnFailed(event -> {
            if (requestId != ctx.reparseRequestId()[0]) {
                return;
            }
            setFailedUi(ctx);
        });
        task.setOnCancelled(event -> {
            if (requestId != ctx.reparseRequestId()[0]) {
                return;
            }
            setIdleUi(ctx.parseProgressBar(), ctx.parseProgressLabel(), ctx.reparseButton(),
                    ctx.appendButton(), ctx.replaceButton());
        });
    }

    private static void startParseThread(Task<?> task) {
        Thread parseThread = new Thread(task, "schedule-paste-parse-thread");
        parseThread.setDaemon(true);
        parseThread.start();
    }

    private static Runnable createImportRowsRunnable(TableView<PreviewRow> table, ScheduleController scheduleController,
                                                     Stage stage) {
        return () -> {
            for (PreviewRow r : table.getItems()) {
                if (r.getEntry().getKind() == SchedulePasteEntry.Kind.SECTION) {
                    scheduleController.addSectionRow(r.getEntry().getText());
                } else if (r.getMatchedSong() != null) {
                    scheduleController.addSong(r.getMatchedSong());
                }
            }
            scheduleController.getListView().refresh();
            stage.close();
        };
    }

    public static void show(ScheduleController scheduleController, SongController songController) {
        Settings settings = Settings.getInstance();
        ResourceBundle bundle = settings.getResourceBundle();

        TextArea textArea = createClipboardTextArea();

        Button reparseButton = new Button(bundle.getString("Reparse"));
        ProgressBar parseProgressBar = new ProgressBar(0);
        parseProgressBar.setPrefWidth(180);
        SceneUtils.setVisibleAndManaged(parseProgressBar, false);
        Label parseProgressLabel = new Label();
        SceneUtils.setVisibleAndManaged(parseProgressLabel, false);
        HBox topBar = createTopBar(reparseButton, parseProgressBar, parseProgressLabel);

        TableUi tableUi = createTableUi(bundle);
        TableView<PreviewRow> table = tableUi.table;
        NotFoundUi notFoundUi = createNotFoundUi(bundle);
        Runnable refreshNotFound = createRefreshNotFoundRunnable(table, notFoundUi);
        configureSongColumn(tableUi.songCol, table, refreshNotFound);

        Button appendButton = new Button(bundle.getString("Append to schedule"));
        Button replaceButton = new Button(bundle.getString("Replace schedule"));
        Button cancelButton = new Button(bundle.getString("Cancel"));
        HBox buttons = createButtonsBar(appendButton, replaceButton, cancelButton);

        @SuppressWarnings("unchecked") final Task<ObservableList<PreviewRow>>[] currentReparseTask = new Task[]{null};
        final long[] reparseRequestId = new long[]{0};

        Runnable launchReparse = createLaunchReparseRunnable(
                textArea, songController, table, refreshNotFound, parseProgressBar, parseProgressLabel,
                reparseButton, appendButton, replaceButton, bundle, currentReparseTask, reparseRequestId);

        reparseButton.setOnAction(e -> launchReparse.run());
        if (textArea.getText() != null && !textArea.getText().isBlank()) {
            launchReparse.run();
        }

        VBox root = new VBox(8, topBar, textArea, table, notFoundUi.box, buttons);
        root.setPrefSize(780, 620);
        root.setPadding(new Insets(4));
        VBox.setVgrow(textArea, Priority.ALWAYS);
        VBox.setVgrow(table, Priority.ALWAYS);

        Stage stage = SceneUtils.getCustomStage3(SchedulePasteImportDialog.class, root);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(bundle.getString("Import schedule from clipboard"));

        Runnable importRows = createImportRowsRunnable(table, scheduleController, stage);

        appendButton.setOnAction(e -> importRows.run());
        replaceButton.setOnAction(e -> {
            scheduleController.getListView().getItems().clear();
            importRows.run();
        });
        cancelButton.setOnAction(e -> {
            Task<ObservableList<PreviewRow>> runningTask = currentReparseTask[0];
            if (runningTask != null && runningTask.isRunning()) {
                runningTask.cancel();
            }
            stage.close();
        });

        stage.showAndWait();
    }

    public static class PreviewRow {
        private final int rowNumber;
        private final SchedulePasteEntry entry;
        private final List<Song> rankedSongs;
        private final ResourceBundle bundle;
        private Song matchedSong;
        private double matchConfidence;
        private String statusText;
        private String filterText = "";

        private PreviewRow(int rowNumber, SchedulePasteEntry entry, Song matchedSong,
                           double matchConfidence, List<Song> rankedSongs, ResourceBundle bundle) {
            this.rowNumber = rowNumber;
            this.entry = entry;
            this.matchedSong = matchedSong;
            this.matchConfidence = matchConfidence;
            this.rankedSongs = rankedSongs;
            this.bundle = bundle;
        }

        static PreviewRow section(int rowNumber, SchedulePasteEntry entry, ResourceBundle bundle) {
            PreviewRow row = new PreviewRow(rowNumber, entry, null, -1, List.of(), bundle);
            row.statusText = bundle.getString("Schedule status section");
            return row;
        }

        static PreviewRow song(int rowNumber, SchedulePasteEntry entry, ScheduleSongMatcher.Result result,
                               List<Song> sourceSongs, ResourceBundle bundle) {
            Song song = result.song();
            double confidence = result.confidence();
            switch (result.status()) {
                case MATCHED, AMBIGUOUS:
                    break;
                case NOT_FOUND:
                default:
                    song = null;
                    break;
            }
            PreviewRow row = new PreviewRow(rowNumber, entry, song, confidence,
                    TitleSimilarity.rankSongs(entry.getText(), sourceSongs), bundle);
            row.refreshStatusText(confidence);
            return row;
        }

        void refreshStatusText(double confidence) {
            if (entry.getKind() == SchedulePasteEntry.Kind.SECTION) {
                return;
            }
            this.matchConfidence = confidence;
            if (matchedSong != null) {
                statusText = confidence >= ScheduleSongMatcher.AUTO_MATCH_THRESHOLD
                        ? bundle.getString("Schedule status matched")
                        : bundle.getString("Schedule status ambiguous");
            } else {
                statusText = bundle.getString("Schedule status not found");
            }
        }

        public SchedulePasteEntry getEntry() {
            return entry;
        }

        @SuppressWarnings("unused")
        public int getRowNumber() {
            return rowNumber;
        }

        public String getParsedText() {
            return entry.getText();
        }

        public Song getMatchedSong() {
            return matchedSong;
        }

        public void setMatchedSong(Song matchedSong) {
            this.matchedSong = matchedSong;
            this.matchConfidence = matchedSong != null ? TitleSimilarity.score(entry.getText(), matchedSong) : 0;
        }

        @SuppressWarnings("unused")
        public String getStatusText() {
            return statusText;
        }

        public double getMatchConfidence() {
            return matchConfidence;
        }

        public List<Song> getRankedSongs() {
            return rankedSongs;
        }

        public String getFilterText() {
            return filterText;
        }

        public void setFilterText(String filterText) {
            this.filterText = filterText;
        }
    }
}
