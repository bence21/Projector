package projector.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import projector.application.ProjectionScreenSettings;
import projector.model.Bible;

import java.util.ArrayList;
import java.util.List;

import static projector.controller.ParallelBiblesController.sortParallelBibles;

public class ProjectionParallelBiblesController {
    @FXML
    private VBox listView;
    private List<Bible> bibles;
    private List<ProjectionParallelBibleHolder> holders = new ArrayList<>();
    private ProjectionScreenSettings projectionScreenSettings;

    public void initialize(List<Bible> bibles, ProjectionScreenSettings projectionScreenSettings, Stage stage) {
        this.projectionScreenSettings = projectionScreenSettings;
        this.bibles = new ArrayList<>(bibles);
        sortParallelBibles(this.bibles);
        holders = new ArrayList<>(this.bibles.size());
        for (Bible bible : this.bibles) {
            addBibleToVBox(bible);
        }
        setStage(stage);
    }

    private void addBibleToVBox(Bible bible) {
        CheckBox checkBox = new CheckBox(bible.getName() + " - " + bible.getShortName());
        boolean selected = !projectionScreenSettings.isSkipped(bible);
        checkBox.setSelected(selected);
        Platform.runLater(() -> listView.getChildren().addAll(checkBox));
        ProjectionParallelBibleHolder bibleHolder = new ProjectionParallelBibleHolder();
        bibleHolder.checkBox = checkBox;
        holders.add(bibleHolder);
    }

    public void setStage(Stage stage) {
        stage.setOnCloseRequest(event -> {
            int i = 0;
            for (Bible bible : bibles) {
                ProjectionParallelBibleHolder bibleHolder = holders.get(i);
                projectionScreenSettings.handleBibleSkipping(bible, !bibleHolder.checkBox.isSelected());
                ++i;
            }
            projectionScreenSettings.save();
        });
    }

    static class ProjectionParallelBibleHolder {
        public CheckBox checkBox;
    }
}
